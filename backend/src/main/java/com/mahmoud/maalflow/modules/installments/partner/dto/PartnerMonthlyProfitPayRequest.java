package com.mahmoud.maalflow.modules.installments.partner.dto;

import com.mahmoud.maalflow.modules.installments.payment.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PartnerMonthlyProfitPayRequest {

    @NotNull(message = "{messages.partner.userId.required}")
    private Long paidByUserId;

    /**
     * Amount to pay out now. If less than calculatedProfit, the remaining amount will be reinvested.
     * If null, backend treats it as paying the full calculatedProfit (backward-compatible).
     */
    @DecimalMin(value = "0.00", message = "{messages.amount.mustBePositive}")
    private BigDecimal payoutAmount;

    private PaymentMethod paymentMethod;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate paymentDate;

    private String notes;
}


