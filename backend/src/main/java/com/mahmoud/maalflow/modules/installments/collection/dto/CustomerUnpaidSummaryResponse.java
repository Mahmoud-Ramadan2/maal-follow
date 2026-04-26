package com.mahmoud.maalflow.modules.installments.collection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Lightweight response for route candidate customers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUnpaidSummaryResponse {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private BigDecimal outstandingAmount;
}

