package com.mahmoud.maalflow.modules.associations.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class AssociationPaymentRequest {
    @NotNull private Long associationId;
    @NotNull private Long memberId;
    @NotNull @Positive private BigDecimal amount;
    @NotNull private String paymentMonth; // YYYY-MM
    @NotNull private LocalDate paymentDate;
    private String notes;
}

