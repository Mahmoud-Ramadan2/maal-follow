package com.mahmoud.maalflow.modules.installments.partner.dto;

import com.mahmoud.maalflow.modules.installments.partner.enums.CommissionType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartnerCommissionRequest {

    @NotNull(message = "{messages.partner.partnerId.required}")
    private Long partnerId;

    @NotNull(message = "{validation.amount.required}")
    @DecimalMin(value = "0.01", message = "{validation.amount.min}")
    @Digits(integer = 12, fraction = 2, message = "{validation.amount.format}")
    private BigDecimal amount;

    @NotNull(message = "{messages.partner.commissionType.required}")
    private CommissionType commissionType;

    @Size(max = 500, message = "{validation.notes.size}")
    private String notes;

    private Long purchaseId;

    private Long contractId;
}
