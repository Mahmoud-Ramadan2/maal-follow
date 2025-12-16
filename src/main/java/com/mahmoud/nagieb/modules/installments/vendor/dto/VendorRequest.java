package com.mahmoud.nagieb.modules.installments.vendor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorRequest {
    @NotBlank(message = "{validation.name.required}")
    @Size(min = 4, max = 50, message = "{validation.name.size}")
    private String name;
    @NotBlank(message = "{validation.phone.required}")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "{validation.phone.pattern}")
    private String phone;
    @NotBlank
    @Size(max = 100, message = "{validation.address.size}")
    private String address;
    private String notes;

}
