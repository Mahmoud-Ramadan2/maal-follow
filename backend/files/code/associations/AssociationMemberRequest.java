package com.mahmoud.maalflow.modules.associations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class AssociationMemberRequest {
    @NotNull private Long associationId;
    @NotBlank private String memberName;
    private String phone;
    @NotNull @Positive private Integer turnOrder;
    private String notes;
}

