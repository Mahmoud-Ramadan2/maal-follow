package com.mahmoud.maalflow.modules.installments.partner.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PartnerMonthlyProfitAdjustRequest {

    @NotNull(message = "{messages.partner.calculatedProfit.required}")
    @DecimalMin(value = "0.00", message = "{messages.partner.calculatedProfit.min}")
    @Digits(integer = 12, fraction = 2, message = "{messages.partner.calculatedProfit.format}")
    private BigDecimal newAmount;

    @NotBlank(message = "{messages.partner.reason.required}")
    private String reason;
}


