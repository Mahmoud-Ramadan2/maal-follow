package com.mahmoud.maalflow.modules.installments.payment.dto;

import com.mahmoud.maalflow.modules.installments.payment.enums.PaymentMethod;
import com.mahmoud.maalflow.modules.installments.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Summary DTO for payment listing.
 *
 * @author Mahmoud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSummary {

    private Long id;
    private BigDecimal amount;
    private BigDecimal netAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDate actualPaymentDate;
    private String agreedPaymentMonth;
    private Boolean isEarlyPayment;
    private LocalDateTime createdAt;
}

