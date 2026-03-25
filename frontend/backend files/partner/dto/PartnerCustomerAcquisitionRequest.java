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

    @NotNull(message = "validation.partner.id.required")
    private Long partnerId;

    @NotNull(message = "validation.customer.id.required")
    private Long customerId;

    @NotNull(message = "validation.commission.percentage.required")
    @DecimalMin(value = "0.0", message = "validation.commission.percentage.min")
    @DecimalMax(value = "100.0", message = "validation.commission.percentage.max")
    private BigDecimal commissionPercentage;

    private String acquisitionNotes;
}
