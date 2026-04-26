package com.mahmoud.maalflow.modules.installments.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Aggregated report response for filtered payment datasets.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReportResponse {

    private String month;
    private LocalDate startDate;
    private LocalDate endDate;

    private int totalCount;
    private int completedCount;
    private int cancelledCount;
    private int refundedCount;
    private int earlyPaymentCount;

    private BigDecimal totalAmount;
    private BigDecimal totalNetAmount;
    private BigDecimal totalDiscounts;
}

