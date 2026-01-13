package com.mahmoud.nagieb.modules.installments.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for daily payment summary statistics.
 *
 * @author Mahmoud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyPaymentSummary {

    private LocalDate date;
    private Long paymentCount;
    private BigDecimal totalAmount;
}

