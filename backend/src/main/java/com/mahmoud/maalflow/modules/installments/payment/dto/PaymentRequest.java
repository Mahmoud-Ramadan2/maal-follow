
package com.mahmoud.maalflow.modules.installments.payment.dto;

import com.mahmoud.maalflow.modules.installments.payment.enums.PaymentMethod;
import com.mahmoud.maalflow.modules.installments.payment.enums.PaymentStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating/processing payments.
 * Includes idempotency key for duplicate prevention.
 *
 * @author Mahmoud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    /**
     * Idempotency key to prevent duplicate payment processing.
     */
    @NotBlank(message = "{validation.payment.idempotencyKey.required}")
    @Size(max = 100, message = "{validation.payment.idempotencyKey.size}")
    private String idempotencyKey;

    private Long installmentScheduleId;

    @NotNull(message = "{validation.payment.amount.required}")
    @DecimalMin(value = "0.01", message = "{validation.payment.amount.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.payment.amount.format}")
    private BigDecimal amount;

    @NotNull(message = "{validation.payment.paymentMethod.required}")
    private PaymentMethod paymentMethod;

    @NotNull(message = "{validation.payment.actualPaymentDate.required}")
    private LocalDate actualPaymentDate;

    private Long receiptDocumentId;

    private PaymentStatus status;

    @Digits(integer = 10, fraction = 2, message = "{validation.payment.amount.format}")
    BigDecimal extraExpenses;

//    @NotBlank(message = "{validation.payment.agreedPaymentMonth.required}")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "{validation.payment.agreedPaymentMonth.format}")
    private String agreedPaymentMonth;




    /**
     * Whether this is an early payment (before due date).
     */
    private Boolean isEarlyPayment = false;

    /**
     * Discount amount for early payment.
     */
    @DecimalMin(value = "0.00", message = "{validation.payment.discountAmount.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.payment.discountAmount.format}")
    private BigDecimal discountAmount;

    @Size(max = 500, message = "{validation.notes.size}")
    private String notes;

    /**
     * Collector user ID (optional, defaults to current user).
     */
    private Long collectorId;
}

