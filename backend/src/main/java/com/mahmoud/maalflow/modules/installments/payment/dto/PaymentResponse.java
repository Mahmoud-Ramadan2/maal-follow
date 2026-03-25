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
 * Response DTO for payment details.
 *
 * @author Mahmoud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long id;
    private String idempotencyKey;
    private Long installmentScheduleId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private LocalDate actualPaymentDate;
    private String agreedPaymentMonth;
    private Boolean isEarlyPayment;
    private BigDecimal discountAmount;
    private BigDecimal netAmount;
    private String notes;
    private LocalDateTime createdAt;
    private String receivedByName;
    private Long receivedById;
    private String collectorName;
    private Long collectorId;
}

