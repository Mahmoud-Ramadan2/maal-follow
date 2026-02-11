package com.mahmoud.nagieb.modules.installments.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for daily payment summaries.
 */
@Data
public class DailyPaymentSummary {

    private LocalDate paymentDate;
    private int paymentCount;
    private BigDecimal totalAmount;
    private BigDecimal averagePayment;
}
