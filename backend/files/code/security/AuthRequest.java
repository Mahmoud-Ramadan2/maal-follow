package com.mahmoud.maalflow.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login request DTO.
 * Copy to: src/main/java/com/mahmoud/maalflow/security/dto/AuthRequest.java
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    @NotBlank @Email
    private String email;
    @NotBlank
    private String password;
}

