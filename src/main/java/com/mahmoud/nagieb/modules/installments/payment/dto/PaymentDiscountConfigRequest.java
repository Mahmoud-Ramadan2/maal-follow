package com.mahmoud.nagieb.modules.installments.payment.dto;

import com.mahmoud.nagieb.modules.installments.payment.enums.DiscountType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for payment discount configuration.
 */
@Data
public class PaymentDiscountConfigRequest {

    @NotNull(message = "validation.discount.type.required")
    private DiscountType discountType;

    @Positive(message = "validation.discount.threshold.invalid")
    private Integer earlyPaymentDaysThreshold = 5;

    @DecimalMin(value = "0.0", message = "validation.discount.percentage.invalid")
    @DecimalMax(value = "100.0", message = "validation.discount.percentage.invalid")
    private BigDecimal earlyPaymentDiscountPercentage = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "validation.discount.percentage.invalid")
    @DecimalMax(value = "100.0", message = "validation.discount.percentage.invalid")
    private BigDecimal finalInstallmentDiscountPercentage = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "validation.discount.amount.invalid")
    private BigDecimal minimumDiscountAmount;

    @DecimalMin(value = "0.0", message = "validation.discount.amount.invalid")
    private BigDecimal maximumDiscountAmount;

    private String description;
}
