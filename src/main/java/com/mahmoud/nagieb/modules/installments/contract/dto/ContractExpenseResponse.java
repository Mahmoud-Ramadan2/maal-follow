package com.mahmoud.nagieb.modules.installments.contract.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mahmoud.nagieb.modules.shared.enums.ExpenseType;
import com.mahmoud.nagieb.modules.shared.enums.PaidBy;
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

    private String contractNumber;
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

