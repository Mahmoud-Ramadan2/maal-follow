package com.mahmoud.maalflow.modules.debts.dto;

import com.mahmoud.maalflow.modules.debts.enums.DebtStatus;
import com.mahmoud.maalflow.modules.debts.enums.DebtType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DebtResponse {
    private Long id;
    private String personName;
    private String phone;
    private DebtType debtType;
    private BigDecimal originalAmount;
    private BigDecimal remainingAmount;
    private BigDecimal totalPaid;
    private String description;
    private LocalDate dueDate;
    private DebtStatus status;
    private LocalDateTime createdAt;
    private int paymentCount;
}

