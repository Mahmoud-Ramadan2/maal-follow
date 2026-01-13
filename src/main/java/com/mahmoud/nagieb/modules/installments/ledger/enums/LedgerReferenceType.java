package com.mahmoud.nagieb.modules.installments.ledger.enums;

/**
 * Reference type for ledger entries.
 *
 * @author Mahmoud
 */
public enum LedgerReferenceType {
    PAYMENT,              // References payment table
    PURCHASE,             // References product_purchase table
    INVESTMENT,           // References partner_investment table
    WITHDRAWAL,           //  References partner_withdrawal table
    PROFIT_DISTRIBUTION,  //  References partner_monthly_profit table
    CONTRACT_EXPENSE,     // References contract_expense table
    MANUAL               // No specific reference
}


