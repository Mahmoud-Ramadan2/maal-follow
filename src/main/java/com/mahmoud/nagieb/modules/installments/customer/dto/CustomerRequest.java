package com.mahmoud.nagieb.modules.installments.customer.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * @author Mahmoud
 */
@Data
public class CustomerRequest {

    @NotBlank(message = "{validation.name.required}")
    @Size(min = 4, max = 50, message = "{validation.name.size}")
    private String name;
    @NotBlank(message = "{validation.phone.required}")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "{validation.phone.pattern}")
    private String phone;
    @NotBlank(message = "{validation.address.required}")
    @Size(max = 100, message = "{validation.address.size}")
    private String address;
    @NotNull(message ="{validation.nationalId.required}")
    @Min(value = 100000L, message = "{validation.nationalId.size}")
    @Max(value = 99999999999999L, message = "{validation.nationalId.size}")
    private Long nationalId;
    private String notes;

    public CustomerRequest() {
    }
    public CustomerRequest( String name, String phone, String address, Long nationalId, String notes){
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.nationalId = nationalId;
        this.notes = notes;
    }
}
