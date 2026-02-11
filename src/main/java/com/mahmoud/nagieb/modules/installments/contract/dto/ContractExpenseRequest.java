package com.mahmoud.nagieb.modules.installments.contract.dto;

import com.mahmoud.nagieb.modules.installments.contract.enums.ExpenseType;
import com.mahmoud.nagieb.modules.installments.contract.enums.PaidBy;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractExpenseRequest {

    @Positive
    private Long contractId;

    @Positive
    private Long scheduleId;

    @NotNull(message = "{messages.contract.expenseType.required}")
    private ExpenseType expenseType;

    @NotNull(message = "{messages.contract.amount.required}")
    @DecimalMin(value = "0.01", message = "{messages.contract.amount.min}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal amount;

    @Size(max = 255, message = "{messages.contract.description.size}")
    private String description;

    @NotNull(message = "{messages.contract.expenseDate.required}")
    private LocalDate expenseDate;

    private PaidBy paidBy;

    @Positive
    private Long partnerId;

    @Size(max = 100, message = "{messages.contract.receiptNumber.size}")
    private String receiptNumber;

    @Size(max = 500, message = "{messages.contract.notes.size}")
    private String notes;
}

