package com.mahmoud.maalflow.modules.installments.partner.enums;

/**
 * Profit distribution and payment status.
 */
public enum ProfitStatus {
    CALCULATED,
    PAID,
    REINVESTED,
    /**
     * Profit was settled using a mix of direct payout + reinvestment.
     * (Example: calculated=1127, paid=1000, reinvested=127)
     */
    PARTIALLY_SETTLED,
    DEFERRED
}