package com.mahmoud.maalflow.modules.installments.partner.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PartnerProfitConfigRequest {

    @NotNull(message = "{messages.partner.profitCalculation.managementFee.required}")
    @DecimalMin(value = "0.00", message = "{messages.partner.profitCalculation.managementFee.invalid}")
    @DecimalMax(value = "100.00", message = "{messages.partner.profitCalculation.managementFee.invalid}")
    private BigDecimal managementFeePercentage;

    @NotNull(message = "{messages.partner.profitCalculation.zakat.required}")
    @DecimalMin(value = "0.00", message = "{messages.partner.profitCalculation.zakat.invalid}")
    @DecimalMax(value = "100.00", message = "{messages.partner.profitCalculation.zakat.invalid}")
    private BigDecimal zakatPercentage;

    @NotNull(message = "{messages.partner.profitCalculation.paymentDay.required}")
    private Integer profitPaymentDay;
}


