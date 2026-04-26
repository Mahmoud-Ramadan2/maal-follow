package com.mahmoud.maalflow.modules.installments.partner.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PartnerSalesCommissionRequest {

    @NotNull(message = "{messages.partner.partnerId.required}")
    private Long partnerId;

    @NotNull(message = "{messages.partner.contractId.required}")
    private Long contractId;
}


