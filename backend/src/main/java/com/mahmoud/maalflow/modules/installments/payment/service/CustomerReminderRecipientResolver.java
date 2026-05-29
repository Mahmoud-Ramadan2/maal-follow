package com.mahmoud.maalflow.modules.installments.payment.service;

import com.mahmoud.maalflow.modules.installments.customer.entity.Customer;
import com.mahmoud.maalflow.modules.installments.payment.dto.ReminderRecipient;
import com.mahmoud.maalflow.modules.installments.schedule.entity.InstallmentSchedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerReminderRecipientResolver implements ReminderRecipientResolver {

    @Override
    public Optional<ReminderRecipient> resolve(InstallmentSchedule schedule) {
        if (schedule == null || schedule.getContract() == null || schedule.getContract().getCustomer() == null) {
            return Optional.empty();
        }

        Customer customer = schedule.getContract().getCustomer();
        return Optional.of(new ReminderRecipient(
                customer.getId(),
                customer.getName(),
                null,
                customer.getPhone()
        ));
    }
}
