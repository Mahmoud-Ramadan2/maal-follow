package com.mahmoud.maalflow.modules.installments.partner.dto;

import com.mahmoud.maalflow.modules.installments.partner.enums.CommissionStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartnerProfitSharingRequest {

    @NotNull(message = "{validation.partnerId.required}")
    private Long partnerId;

    @NotNull(message = "{validation.amount.required}")
    @DecimalMin(value = "0.01", message = "{validation.amount.min}")
    @Digits(integer = 12, fraction = 2, message = "{validation.amount.format}")
    private BigDecimal amount;

    @NotNull(message = "{validation.sharePercentage.required}")
    @DecimalMin(value = "0.00", message = "{validation.sharePercentage.invalid}")
    @DecimalMax(value = "100.00", message = "{validation.sharePercentage.invalid}")
    @Digits(integer = 5, fraction = 2, message = "{validation.sharePercentage.format}")
    private BigDecimal sharePercentage;

    private CommissionStatus status;

    @Size(max = 500, message = "{validation.notes.size}")
    private String notes;

    private Long contractId;

    private Long paymentId;
}