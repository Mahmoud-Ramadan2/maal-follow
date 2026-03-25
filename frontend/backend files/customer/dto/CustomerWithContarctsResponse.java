package com.mahmoud.maalflow.modules.installments.customer.dto;

import com.mahmoud.maalflow.modules.installments.contract.dto.ContractResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for customer with all their contracts.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerWithContarctsResponse {

    private Long id;
    private String name;
    private String phone;
    private String address;
    private String nationalId;
    private String notes;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private List<ContractResponse> contracts;
}
