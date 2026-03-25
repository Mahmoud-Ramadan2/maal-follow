package com.mahmoud.maalflow.modules.installments.partner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for partner commission summary statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerCommissionSummary {

    private Long partnerId;
    private BigDecimal pendingAmount;
    private BigDecimal paidAmount;
    private BigDecimal totalAmount;
    private long pendingCount;
    private long paidCount;
    private long totalCount;
}
