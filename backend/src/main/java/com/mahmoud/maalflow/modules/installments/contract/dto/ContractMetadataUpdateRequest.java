package com.mahmoud.maalflow.modules.installments.contract.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractMetadataUpdateRequest {

    @Size(max = 500, message = "{validation.notes.size}")
    private String notes;

    private Long responsibleUserId;
    private Boolean clearResponsibleUser;

    private Long partnerId;
    private Boolean clearPartner;
}
