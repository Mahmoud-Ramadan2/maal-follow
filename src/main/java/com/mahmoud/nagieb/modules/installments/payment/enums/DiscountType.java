package com.mahmoud.nagieb.modules.installments.payment.enums;

/**
 * Types of payment discounts available.
 */
public enum DiscountType {
    EARLY_PAYMENT,      // Discount for paying before due date
    FINAL_INSTALLMENT,  // Discount for final payment
    BULK_PAYMENT,       // Discount for paying multiple installments at once
    LOYALTY_DISCOUNT,   // Discount for loyal customers
    MANUAL              // Manual discount applied by staff
}
