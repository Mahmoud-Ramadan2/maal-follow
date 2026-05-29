package com.mahmoud.maalflow.modules.installments.payment.repo;

import com.mahmoud.maalflow.modules.installments.schedule.entity.InstallmentSchedule;
import com.mahmoud.maalflow.modules.installments.payment.entity.PaymentReminder;
import com.mahmoud.maalflow.modules.installments.payment.enums.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for PaymentReminder entity.
 */
@Repository
public interface PaymentReminderRepository extends JpaRepository<PaymentReminder, Long> {


    List<PaymentReminder> findByStatusAndReminderDateLessThanEqual(ReminderStatus status, LocalDate date);

    List<PaymentReminder> findByInstallmentScheduleIdAndStatusInAndIsRecurringTrue(Long scheduleId, List<ReminderStatus> statuses);

    List<PaymentReminder> findByInstallmentScheduleId(Long scheduleId);

    @Query("""
            SELECT pr FROM PaymentReminder pr
            WHERE pr.dueDate < :today
                         AND pr.status
            IN ('SENT') AND pr.isRecurring = true
                        AND (
                            pr.lastAttemptAt IS NULL
                            OR pr.lastAttemptAt <= :retryBefore
                        )
            """)
    List<PaymentReminder> findOverdueRemindersAndRecurringTrue(@Param("today") LocalDate today, @Param("retryBefore") LocalDateTime retryBefore);

    boolean existsByInstallmentSchedule(InstallmentSchedule schedule);
}
