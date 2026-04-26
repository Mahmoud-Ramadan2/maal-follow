package com.mahmoud.maalflow.modules.installments.payment.enums;

/**
 * Payment transaction lifecycle status.
 *
 * Note: this enum is for payment records only and is intentionally
 * separate from installment schedule status
 * ({@code com.mahmoud.maalflow.modules.installments.contract.enums.PaymentStatus}).
 *
 * @author Mahmoud
 */
public enum PaymentProcessingStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED
}

