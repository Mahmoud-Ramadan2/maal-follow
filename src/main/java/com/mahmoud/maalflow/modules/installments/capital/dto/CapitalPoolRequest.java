package com.mahmoud.maalflow.modules.installments.capital.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for CapitalPool operations.
 */
@Data
public class CapitalPoolRequest {

    @NotNull(message = "validation.capitalPool.totalAmount.required")
    @PositiveOrZero(message = "validation.capitalPool.totalAmount.positive")
    private BigDecimal totalAmount;

    @NotNull(message = "validation.capitalPool.ownerContribution.required")
    @PositiveOrZero(message = "validation.capitalPool.ownerContribution.positive")
    private BigDecimal ownerContribution;

    @NotNull(message = "validation.capitalPool.partnerContributions.required")
    @PositiveOrZero(message = "validation.capitalPool.partnerContributions.positive")
    private BigDecimal partnerContributions;

    private String description;
}
