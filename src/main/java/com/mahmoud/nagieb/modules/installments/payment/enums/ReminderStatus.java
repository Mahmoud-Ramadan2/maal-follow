package com.mahmoud.nagieb.modules.installments.payment.enums;

/**
 * Status of payment reminders.
 */
public enum ReminderStatus {
    PENDING,        // Reminder scheduled but not sent yet
    SENT,           // Reminder has been sent
    ACKNOWLEDGED,   // Customer acknowledged the reminder
    COMPLETED,      // Payment received, reminder no longer needed
    CANCELLED,      // Reminder cancelled
    FAILED          // Failed to send reminder
}
