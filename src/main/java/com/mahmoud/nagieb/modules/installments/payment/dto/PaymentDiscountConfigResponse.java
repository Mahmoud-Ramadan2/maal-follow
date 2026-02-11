package com.mahmoud.nagieb.modules.installments.payment.dto;

import com.mahmoud.nagieb.modules.installments.payment.enums.DiscountType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for payment discount configuration.
 */
@Data
public class PaymentDiscountConfigResponse {

    private Long id;
    private DiscountType discountType;
    private Integer earlyPaymentDaysThreshold;
    private BigDecimal earlyPaymentDiscountPercentage;
    private BigDecimal finalInstallmentDiscountPercentage;
    private BigDecimal minimumDiscountAmount;
    private BigDecimal maximumDiscountAmount;
    private Boolean isActive;
    private String description;
    private LocalDateTime createdAt;
    private String createdByName;
}
