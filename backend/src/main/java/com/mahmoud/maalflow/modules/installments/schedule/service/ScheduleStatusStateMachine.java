package com.mahmoud.maalflow.modules.installments.schedule.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.contract.enums.PaymentStatus;
import com.mahmoud.maalflow.modules.installments.schedule.entity.InstallmentSchedule;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Enforces allowed installment schedule status transitions.
 */
@Component
public class ScheduleStatusStateMachine {

    private static final Map<PaymentStatus, Set<PaymentStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(PaymentStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(PaymentStatus.PENDING, EnumSet.of(
                PaymentStatus.PARTIALLY_PAID,
                PaymentStatus.PAID,
                PaymentStatus.LATE,
                PaymentStatus.CANCELLED
        ));
        ALLOWED_TRANSITIONS.put(PaymentStatus.LATE, EnumSet.of(
                PaymentStatus.PARTIALLY_PAID,
                PaymentStatus.PAID,
                PaymentStatus.CANCELLED
        ));
        ALLOWED_TRANSITIONS.put(PaymentStatus.PARTIALLY_PAID, EnumSet.of(
                PaymentStatus.PAID,
                PaymentStatus.CANCELLED
        ));
        ALLOWED_TRANSITIONS.put(PaymentStatus.PAID, EnumSet.of(PaymentStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(PaymentStatus.CANCELLED, EnumSet.noneOf(PaymentStatus.class));
    }

    public void transition(InstallmentSchedule schedule, PaymentStatus targetStatus) {
        PaymentStatus currentStatus = schedule.getStatus() != null ? schedule.getStatus() : PaymentStatus.PENDING;
        validateTransition(currentStatus, targetStatus);
        schedule.setStatus(targetStatus);
    }

    public void validateTransition(PaymentStatus currentStatus, PaymentStatus targetStatus) {
        if (targetStatus == null) {
            throw new BusinessException("messages.schedule.statusTransition.invalid");
        }
        if (currentStatus == null || currentStatus == targetStatus) {
            return;
        }
        if (targetStatus == PaymentStatus.CANCELLED) {
            return;
        }
        Set<PaymentStatus> allowedTargets = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, EnumSet.noneOf(PaymentStatus.class));
        if (!allowedTargets.contains(targetStatus)) {
            throw new BusinessException("messages.schedule.statusTransition.invalid");
        }
    }
}

