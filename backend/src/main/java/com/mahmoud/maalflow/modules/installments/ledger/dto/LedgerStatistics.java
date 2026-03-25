package com.mahmoud.maalflow.modules.installments.ledger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for overall ledger statistics.
 *
 * @author Mahmoud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerStatistics {

    private Long totalEntries;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private Long incomeEntries;
    private Long expenseEntries;
    private BigDecimal incomeThisMonth;
    private BigDecimal expensesThisMonth;
    private BigDecimal netBalanceThisMonth;
}

