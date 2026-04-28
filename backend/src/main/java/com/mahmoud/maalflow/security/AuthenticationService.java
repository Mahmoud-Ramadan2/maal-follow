package com.mahmoud.maalflow.security;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import com.mahmoud.maalflow.security.dto.AuthRequest;
import com.mahmoud.maalflow.security.dto.AuthResponse;
import com.mahmoud.maalflow.security.dto.RefreshTokenRequest;
import com.mahmoud.maalflow.security.entity.RefreshToken;
import com.mahmoud.maalflow.security.repo.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public AuthResponse login(AuthRequest request) {
        String email = normalizeEmail(request.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("messages.user.notFound"));

        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails, user.getId(), user.getRole().name());

        String familyId = UUID.randomUUID().toString();
        String refreshJti = jwtService.generateRefreshJti();
        String refreshToken = jwtService.generateRefreshToken(userDetails, user.getId(), familyId, null, refreshJti);

        persistRefreshToken(user, refreshToken, refreshJti, familyId, null, null, null);

        log.info("User logged in: {}", user.getEmail());
        return buildResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        try {
            String email = jwtService.extractUsername(refreshToken);
            String jti = jwtService.extractJti(refreshToken);
            String familyId = jwtService.extractFamilyId(refreshToken);

            var userDetails = userDetailsService.loadUserByUsername(email);
            if (!jwtService.isRefreshTokenValid(refreshToken, userDetails)) {
                throw new BusinessException("messages.auth.invalidRefreshToken");
            }

            RefreshToken existing = refreshTokenRepository.findByJti(jti)
                    .orElseThrow(() -> new BusinessException("messages.auth.invalidRefreshToken"));

            if (!passwordEncoder.matches(refreshToken, existing.getTokenHash())
                    || existing.getRevokedAt() != null
                    || existing.getReplacedByJti() != null
                    || existing.isReuseDetected()) {
                revokeFamily(existing.getFamilyId());
                throw new BusinessException("messages.auth.invalidRefreshToken");
            }

            User user = existing.getUser();
            String accessToken = jwtService.generateAccessToken(userDetails, user.getId(), user.getRole().name());

            String newJti = jwtService.generateRefreshJti();
            String newRefreshToken = jwtService.generateRefreshToken(userDetails, user.getId(), familyId, jti, newJti);
            persistRefreshToken(user, newRefreshToken, newJti, familyId, jti, null, null);

            existing.setRevokedAt(LocalDateTime.now());
            existing.setReplacedByJti(newJti);
            refreshTokenRepository.save(existing);

            return buildResponse(user, accessToken, newRefreshToken);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("messages.auth.invalidRefreshToken");
        }
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            String jti = jwtService.extractJti(refreshToken);

            RefreshToken existing = refreshTokenRepository.findByJti(jti)
                    .orElseThrow(() -> new BusinessException("messages.auth.invalidRefreshToken"));

            existing.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(existing);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("messages.auth.invalidRefreshToken");
        }
    }

    @Transactional
    public void logoutAll(RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            String email = jwtService.extractUsername(refreshToken);
            var userDetails = userDetailsService.loadUserByUsername(email);

            if (!jwtService.isRefreshTokenValid(refreshToken, userDetails)) {
                throw new BusinessException("messages.auth.invalidRefreshToken");
            }

            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new BusinessException("messages.user.notFound"));
            List<RefreshToken> tokens = refreshTokenRepository.findAllByUser_Id(user.getId());
            LocalDateTime now = LocalDateTime.now();
            for (RefreshToken token : tokens) {
                if (token.getRevokedAt() == null) {
                    token.setRevokedAt(now);
                    token.setReuseDetected(true);
                }
            }
            refreshTokenRepository.saveAll(tokens);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("messages.auth.invalidRefreshToken");
        }
    }

    private void persistRefreshToken(User user, String rawToken, String jti, String familyId, String parentJti,
                                     String deviceId, String userAgent) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(passwordEncoder.encode(rawToken))
                .jti(jti)
                .familyId(familyId)
                .parentJti(parentJti)
                .deviceId(deviceId)
                .ipAddress(null)
                .userAgent(userAgent)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusNanos(jwtService.getRefreshExpirationMillis() * 1_000_000L))
                .reuseDetected(false)
                .build();
        refreshTokenRepository.save(token);
    }

    private void revokeFamily(String familyId) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByFamilyId(familyId);
        LocalDateTime now = LocalDateTime.now();
        for (RefreshToken token : tokens) {
            token.setReuseDetected(true);
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(now);
            }
        }
        refreshTokenRepository.saveAll(tokens);
    }

    private AuthResponse buildResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new BusinessException("validation.email.required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}


