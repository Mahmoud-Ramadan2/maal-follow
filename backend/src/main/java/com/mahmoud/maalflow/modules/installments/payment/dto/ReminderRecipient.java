package com.mahmoud.maalflow.modules.installments.payment.dto;

public record ReminderRecipient(Long customerId, String name, String email, String phone) {
}
