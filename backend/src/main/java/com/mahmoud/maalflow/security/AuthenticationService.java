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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
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
    private final RefreshTokenRevocationService refreshTokenRevocationService;

    @Transactional
    public AuthResponse     login(AuthRequest request) {
        String email = request.getEmail();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new BusinessException("messages.user.notFound"));

            // Load user details for JWT generation (no additional DB query needed if we pass user directly)
            var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            String accessToken = jwtService.generateAccessToken(userDetails, user.getId(), user.getRole().name());

            String familyId = UUID.randomUUID().toString();
            String refreshJti = UUID.randomUUID().toString();
            String refreshToken = jwtService.generateRefreshToken(userDetails, user.getId(), familyId, null, refreshJti);
//            String minToken = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(refreshToken.getBytes()));

            persistRefreshToken(user, refreshToken, refreshJti, familyId, null, null, null);

            log.info("User logged in: {}", user.getEmail());
            return buildResponse(user, accessToken, refreshToken);

        } catch (Exception ex) {
            log.warn("Authentication failed for email: {} - {}", email, ex.getMessage());
            log.info("Failed login attempt for email: {}", email);
            throw new BusinessException("messages.auth.invalidCredentials");
        }

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

            if (!passwordEncoder.matches(hashToken(refreshToken), existing.getTokenHash())
//            if (!refreshToken.equals( existing.getTokenHash())
                    || existing.getRevokedAt() != null
                    || existing.getReplacedByJti() != null
                    || existing.isReuseDetected()) {
                refreshTokenRevocationService.revokeFamily(existing.getFamilyId());
                throw new BusinessException("messages.auth.invalidRefreshToken");
            }

            User user = existing.getUser();
            String accessToken = jwtService.generateAccessToken(userDetails, user.getId(), user.getRole().name());

            String newJti =UUID.randomUUID().toString();
            String newRefreshToken = jwtService.generateRefreshToken(userDetails, user.getId(), familyId, jti, newJti);
            persistRefreshToken(user, newRefreshToken, newJti, familyId, jti, null, null);

            existing.setRevokedAt(LocalDateTime.now());
            existing.setReplacedByJti(newJti);
            refreshTokenRepository.save(existing);

            return buildResponse(user, accessToken, newRefreshToken);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error(  "Unexpected error: {}", ex.getMessage());
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

            String familyId = jwtService.extractFamilyId(refreshToken);
            refreshTokenRevocationService.revokeFamily(familyId);

        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("messages.auth.invalidRefreshToken");
        }
    }

    private void persistRefreshToken(User user, String rawToken, String jti, String familyId, String parentJti,
                                     String deviceId, String userAgent) throws NoSuchAlgorithmException {
        String hashedToken = hashToken(rawToken);
        RefreshToken token = RefreshToken.builder()
                .user(user)
                    .tokenHash(passwordEncoder.encode(hashedToken))
//                    .tokenHash(rawToken)
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

    private String hashToken(String token) throws NoSuchAlgorithmException {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);

    }

}


