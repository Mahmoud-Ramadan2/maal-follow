package com.mahmoud.maalflow.modules.installments.contract.dto;

import com.mahmoud.maalflow.modules.installments.contract.enums.ExpenseType;
import com.mahmoud.maalflow.modules.shared.enums.PaidBy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContractExpenseResponse {

    private  Long id;
    private String contractNumber;
    private Long scheduleId;
    private ExpenseType expenseType;
    private BigDecimal amount;
    private String description;
    private LocalDate expenseDate;
    private PaidBy paidBy;
    private String partnerName;
    private String receiptNumber;
    private String notes;
    private LocalDateTime createdAt;
    private String createdByName;

}

