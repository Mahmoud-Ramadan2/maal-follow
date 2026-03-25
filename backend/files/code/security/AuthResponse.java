package com.mahmoud.maalflow.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication response with JWT tokens.
 * Copy to: src/main/java/com/mahmoud/maalflow/security/dto/AuthResponse.java
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String name;
    private String email;
    private String role;
}

