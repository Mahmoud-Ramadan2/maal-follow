package com.mahmoud.maalflow.modules.shared.user.dto;

import com.mahmoud.maalflow.modules.shared.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank(message = "{validation.name.required}")
    @Size(min = 2, max = 200, message = "{validation.name.size}")
    private String name;

    @Email(message = "{validation.email.invalid}")
    @Size(max = 200, message = "{validation.email.size}")
    private String email;

    @Size(min = 6, max = 100, message = "{validation.password.size}")
    private String password;

    @NotNull(message = "{validation.user.role.required}")
    private UserRole role;

    @Size(max = 20, message = "{validation.phone.size}")
    private String phone;
}

