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

    @NotNull(message = "messages.capitalPool.totalAmount.required")
    @PositiveOrZero(message = "messages.capitalPool.totalAmount.positive")
    private BigDecimal totalAmount;

    @NotNull(message = "messages.capitalPool.ownerContribution.required")
    @PositiveOrZero(message = "messages.capitalPool.ownerContribution.positive")
    private BigDecimal ownerContribution;

    @NotNull(message = "messages.capitalPool.partnerContributions.required")
    @PositiveOrZero(message = "messages.capitalPool.partnerContributions.positive")
    private BigDecimal partnerContributions;

    private String description;
}

