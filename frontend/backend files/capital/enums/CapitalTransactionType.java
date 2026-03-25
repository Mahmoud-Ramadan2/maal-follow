package com.mahmoud.maalflow.modules.installments.capital.enums;

/**
 * Enumeration for capital transaction types.
 * Must match DB ENUM: 'INVESTMENT', 'WITHDRAWAL', 'ALLOCATION', 'RETURN', 'MANUAL'
 *
 * @author Mahmoud
 */
public enum CapitalTransactionType {
    INVESTMENT,
    WITHDRAWAL,
    ALLOCATION,
    RETURN,
    MANUAL
}
