package com.mahmoud.maalflow.modules.installments.customer.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Mahmoud
 */
@Data
public class CustomerResponse {

    private String name;
    private String phone;
    private String address;
    private String nationalId;
    private String notes;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
