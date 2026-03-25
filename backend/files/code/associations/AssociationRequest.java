package com.mahmoud.maalflow.modules.associations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Copy to: src/main/java/com/mahmoud/maalflow/modules/associations/dto/
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class AssociationRequest {
    @NotBlank private String name;
    private String description;
    @NotNull @Positive private BigDecimal monthlyAmount;
    @NotNull @Positive private Integer totalMembers;
    @NotNull private LocalDate startDate;
}

