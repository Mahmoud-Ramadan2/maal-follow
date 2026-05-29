package com.mahmoud.maalflow.modules.installments.payment.service;

import com.mahmoud.maalflow.modules.installments.payment.dto.ReminderRecipient;
import com.mahmoud.maalflow.modules.installments.schedule.entity.InstallmentSchedule;

import java.util.Optional;

public interface ReminderRecipientResolver {

    Optional<ReminderRecipient> resolve(InstallmentSchedule schedule);
}
