package com.mahmoud.maalflow.modules.installments.partner.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for partner customer acquisition operations.
 */
@Data
public class PartnerCustomerAcquisitionRequest {

    @NotNull(message = "messages.partner.partnerId.required")
    private Long partnerId;

    @NotNull(message = "messages.partner.customerId.required")
    private Long customerId;

    @NotNull(message = "messages.partner.commission.percentage.required")
    @DecimalMin(value = "0.0", message = "messages.partner.commission.percentage.min")
    @DecimalMax(value = "100.0", message = "messages.partner.commission.percentage.max")
    private BigDecimal commissionPercentage;

    private String acquisitionNotes;
}

