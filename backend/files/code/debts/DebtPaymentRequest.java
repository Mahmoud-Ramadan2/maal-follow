package com.mahmoud.maalflow.modules.debts.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class DebtPaymentRequest {
    @NotNull private Long debtId;
    @NotNull @Positive private BigDecimal amount;
    @NotNull private LocalDate paymentDate;
    private String paymentMethod;
    private String notes;
}

