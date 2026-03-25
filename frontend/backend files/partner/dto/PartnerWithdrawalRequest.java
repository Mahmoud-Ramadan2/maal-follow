package com.mahmoud.maalflow.modules.installments.partner.dto;

import com.mahmoud.maalflow.modules.installments.partner.enums.WithdrawalType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartnerWithdrawalRequest {

    @NotNull(message = "{validation.partnerId.required}")
    private Long partnerId;

    @NotNull(message = "{validation.amount.required}")
    @DecimalMin(value = "0.01", message = "{validation.amount.min}")
    @Digits(integer = 12, fraction = 2, message = "{validation.amount.format}")
    private BigDecimal amount;

    @NotNull(message = "{validation.withdrawalType.required}")
    private WithdrawalType withdrawalType;

    @Size(max = 500, message = "{validation.requestReason.size}")
    private String requestReason;

    @Size(max = 500, message = "{validation.notes.size}")
    private String notes;
}