package com.mahmoud.maalflow.modules.debts.dto;

import com.mahmoud.maalflow.modules.debts.enums.DebtType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class DebtRequest {
    @NotBlank private String personName;
    private String phone;
    @NotNull private DebtType debtType;
    @NotNull @Positive private BigDecimal originalAmount;
    private String description;
    private LocalDate dueDate;
}

