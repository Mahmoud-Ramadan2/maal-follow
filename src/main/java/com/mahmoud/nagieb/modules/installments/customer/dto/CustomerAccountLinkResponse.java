package com.mahmoud.nagieb.modules.installments.customer.dto;

import com.mahmoud.nagieb.modules.installments.customer.enums.CustomerRelationshipType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for CustomerAccountLink response.
 * @author Mahmoud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAccountLinkResponse {

    private String customerName;

    private String linkedCustomerName;

    private CustomerRelationshipType relationshipType;
    
    private String relationshipDescription;

    private boolean isActive;
    
    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
