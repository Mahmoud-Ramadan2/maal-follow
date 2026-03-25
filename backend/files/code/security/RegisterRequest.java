package com.mahmoud.maalflow.security.dto;

import com.mahmoud.maalflow.modules.shared.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Registration request DTO.
 * Copy to: src/main/java/com/mahmoud/maalflow/security/dto/RegisterRequest.java
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank
    private String name;
    @NotBlank @Email
    private String email;
    @NotBlank @Size(min = 6)
    private String password;
    private String phone;
    private UserRole role;
}

