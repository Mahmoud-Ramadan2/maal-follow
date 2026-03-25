package com.mahmoud.maalflow.security;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.shared.enums.UserRole;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import com.mahmoud.maalflow.security.dto.AuthRequest;
import com.mahmoud.maalflow.security.dto.AuthResponse;
import com.mahmoud.maalflow.security.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service for login, register, and token refresh.
 * Copy to: src/main/java/com/mahmoud/maalflow/security/AuthenticationService.java
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("messages.user.email.exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : UserRole.USER);
        user.setPhone(request.getPhone());

        User saved = userRepository.save(user);
        log.info("Registered new user: {} with role: {}", saved.getEmail(), saved.getRole());

        var userDetails = userDetailsService.loadUserByUsername(saved.getEmail());
        String accessToken = jwtService.generateToken(userDetails, saved.getId(), saved.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(userDetails, saved.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(saved.getRole().name())
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("messages.user.notFound"));

        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails, user.getId(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(userDetails, user.getId());

        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);
        var userDetails = userDetailsService.loadUserByUsername(email);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new BusinessException("messages.auth.invalidRefreshToken");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("messages.user.notFound"));

        String newAccessToken = jwtService.generateToken(userDetails, user.getId(), user.getRole().name());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}

