package com.mahmoud.maalflow.modules.installments.profit.enums;

/**
 * Status of monthly profit distribution calculation and payment.
 */
public enum ProfitDistributionStatus {
    PENDING,      // Not yet calculated
    CALCULATED,   // Calculation complete, ready for distribution
    DISTRIBUTED,  // Profits distributed to partners
    LOCKED        // Locked, cannot be modified
}

