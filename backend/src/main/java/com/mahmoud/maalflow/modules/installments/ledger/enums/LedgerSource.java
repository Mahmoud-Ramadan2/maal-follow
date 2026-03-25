package com.mahmoud.maalflow.modules.installments.ledger.enums;

/**
 * Source of ledger entry.
 *
 * @author Mahmoud
 */
public enum LedgerSource {
    COLLECTION,           // Customer payments
    PURCHASE,             // Product purchases
    INVESTMENT,           // Partner investments
    WITHDRAWAL,           // Partner withdrawals
    PROFIT_DISTRIBUTION,  // Monthly profit payments
    OPERATING_EXPENSE,    // Business expenses
    MANAGEMENT_FEE,       // Management fee income
    ZAKAT,                // Zakat deduction
    DISCOUNT,             // Discount given to customer
    MANUAL                // Manual corrections
}
