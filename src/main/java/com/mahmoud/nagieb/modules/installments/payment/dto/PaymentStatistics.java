package com.mahmoud.nagieb.modules.installments.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for payment statistics and reporting.
* Implements requirement 12: “Calculate the sum of the expected monthly installments and the actually received installments.” */
@Data
@Builder
public class PaymentStatistics {

    private String month;
    private BigDecimal expectedPayments;
    private BigDecimal actualPayments;
    private BigDecimal collectionRate; // Percentage
    private BigDecimal shortfall; // Expected - Actual
    private BigDecimal overduePaid;
    private BigDecimal earlyPayments;
    private BigDecimal totalDiscounts;
    private int totalPaymentCount;
    private int earlyPaymentCount;
    private int overdueCount;
}
