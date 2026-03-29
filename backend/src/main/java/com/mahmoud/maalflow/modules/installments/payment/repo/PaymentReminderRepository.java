package com.mahmoud.maalflow.modules.installments.payment.repo;

import com.mahmoud.maalflow.modules.installments.schedule.entity.InstallmentSchedule;
import com.mahmoud.maalflow.modules.installments.payment.entity.PaymentReminder;
import com.mahmoud.maalflow.modules.installments.payment.enums.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for PaymentReminder entity.
 */
@Repository
public interface PaymentReminderRepository extends JpaRepository<PaymentReminder, Long> {

    boolean existsByInstallmentScheduleAndReminderDate(InstallmentSchedule schedule, LocalDate reminderDate);

    List<PaymentReminder> findByStatusAndReminderDateLessThanEqual(ReminderStatus status, LocalDate date);

    List<PaymentReminder> findByInstallmentScheduleIdAndStatusIn(Long scheduleId, List<ReminderStatus> statuses);

    @Query("SELECT pr FROM PaymentReminder pr WHERE pr.dueDate < :today AND pr.status IN ('SENT', 'PENDING') AND pr.isRecurring = true")
    List<PaymentReminder> findOverdueReminders(@Param("today") LocalDate today);
}
