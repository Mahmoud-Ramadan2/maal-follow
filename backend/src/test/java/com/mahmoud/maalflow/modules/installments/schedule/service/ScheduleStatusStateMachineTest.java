package com.mahmoud.maalflow.modules.installments.schedule.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.contract.enums.PaymentStatus;
import com.mahmoud.maalflow.modules.installments.schedule.entity.InstallmentSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScheduleStatusStateMachineTest {

    private ScheduleStatusStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new ScheduleStatusStateMachine();
    }

    @Test
    void allowsPendingToPaidTransition() {
        InstallmentSchedule schedule = InstallmentSchedule.builder()
                .status(PaymentStatus.PENDING)
                .build();

        stateMachine.transition(schedule, PaymentStatus.PAID);

        assertEquals(PaymentStatus.PAID, schedule.getStatus());
    }

    @Test
    void allowsAnyStatusToCancelledTransition() {
        InstallmentSchedule schedule = InstallmentSchedule.builder()
                .status(PaymentStatus.PAID)
                .build();

        stateMachine.transition(schedule, PaymentStatus.CANCELLED);

        assertEquals(PaymentStatus.CANCELLED, schedule.getStatus());
    }

    @Test
    void rejectsPaidToPendingTransition() {
        InstallmentSchedule schedule = InstallmentSchedule.builder()
                .status(PaymentStatus.PAID)
                .build();

        assertThrows(BusinessException.class,
                () -> stateMachine.transition(schedule, PaymentStatus.PENDING));
    }

    @Test
    void rejectsCancelledToPaidTransition() {
        InstallmentSchedule schedule = InstallmentSchedule.builder()
                .status(PaymentStatus.CANCELLED)
                .build();

        assertThrows(BusinessException.class,
                () -> stateMachine.transition(schedule, PaymentStatus.PAID));
    }
}

