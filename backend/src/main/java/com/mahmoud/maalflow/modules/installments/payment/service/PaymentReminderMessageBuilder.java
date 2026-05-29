package com.mahmoud.maalflow.modules.installments.payment.service;

import com.mahmoud.maalflow.modules.installments.schedule.entity.InstallmentSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentReminderMessageBuilder {

    private final MessageSource messageSource;

    public String buildTitle() {
        return messageSource.getMessage(
                "payment.reminder.title",
                new Object[0],
                "Payment Reminder",
                LocaleContextHolder.getLocale()
        );
    }

    public String buildMessage(InstallmentSchedule schedule) {
        BigDecimal dueAmount = schedule.getAmount().subtract(nz(schedule.getPaidAmount()));
        return messageSource.getMessage(
                "payment.reminder.message",
                new Object[]{schedule.getDueDate().toString(), dueAmount},
                LocaleContextHolder.getLocale()
        );
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0 ? value : BigDecimal.ZERO;
    }
}
