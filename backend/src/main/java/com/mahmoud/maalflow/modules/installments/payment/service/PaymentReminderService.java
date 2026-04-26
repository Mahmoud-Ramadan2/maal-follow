package com.mahmoud.maalflow.modules.installments.payment.service;

import com.mahmoud.maalflow.modules.installments.schedule.entity.InstallmentSchedule;
import com.mahmoud.maalflow.modules.installments.schedule.repo.InstallmentScheduleRepository;
import com.mahmoud.maalflow.modules.installments.payment.entity.PaymentReminder;
import com.mahmoud.maalflow.modules.installments.payment.enums.ReminderStatus;
import com.mahmoud.maalflow.modules.installments.payment.repo.PaymentReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.DEFAULT_REMINDER_DAYS;
import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.MAX_REMINDER_ATTEMPTS;

/**
 * Service for managing payment reminders.
* Implements requirement 17: “Make a reminder to submit the claim five days before it is paid.”
 * Implements requirement 19: “Make a reminder of payment dates to claim on time.”
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentReminderService {

    private final PaymentReminderRepository reminderRepository;
    private final InstallmentScheduleRepository scheduleRepository;
    private MessageSource messageSource;



    /**
     * Create payment reminders for upcoming due dates.
     * Called automatically to generate reminders 5 days before payment due.
     */
    @Transactional
    public void createPaymentReminders() {

        log.info("Creating payment reminders for upcoming due dates");

        LocalDate today = LocalDate.now();
        LocalDate reminderDate = today.plusDays(DEFAULT_REMINDER_DAYS);

        // Get unpaid installments due in 5 days
        List<InstallmentSchedule> upcomingDueSchedules = scheduleRepository
                .findUnpaidInstallmentsDueOnDate(reminderDate);

        int created = 0;
        for (InstallmentSchedule schedule : upcomingDueSchedules) {
            // Check if reminder already exists
            boolean reminderExists = reminderRepository
                    .existsByInstallmentScheduleAndReminderDate(schedule, today);

            if (!reminderExists) {
                createReminderForSchedule(schedule, today, reminderDate);
                created++;
            }
        }

        log.info("Created {} new payment reminders", created);
    }

    /**
     * Send pending reminders (scheduled job).
* Implements: “Make a reminder to submit the claim five days before it is paid.”
     * */

    @Scheduled(cron = "0 00 08 * * ?") // Daily at 8 AM
    @Async
    @Transactional
    public void sendPendingReminders() {
        log.info("Sending pending payment reminders");

        List<PaymentReminder> pendingReminders = reminderRepository
                .findByStatusAndReminderDateLessThanEqual(ReminderStatus.PENDING, LocalDate.now());

        int sent = 0;
        for (PaymentReminder reminder : pendingReminders) {
            if (reminder.getAttemptCount() < MAX_REMINDER_ATTEMPTS) {
                sendReminder(reminder);
                sent++;
            } else {
                log.warn("Max reminder attempts reached for reminder ID: {}", reminder.getId());
                reminder.setStatus(ReminderStatus.FAILED);
                reminderRepository.save(reminder);
            }
        }

        log.info("Sent {} payment reminders", sent);
    }

    /**
     * Send recurring reminders for overdue payments.
     * Continues sending reminders until payment is received.
     */
    @Scheduled(cron = "0 00 08 * * ?") // Daily at 8AM
    @Async
    @Transactional
    public void sendRecurringReminders() {
        log.info("Sending recurring reminders for overdue payments");

        LocalDate today = LocalDate.now();
        List<PaymentReminder> overdueReminders = reminderRepository
                .findOverdueReminders(today);

        int sent = 0;
        for (PaymentReminder reminder : overdueReminders) {
            if (reminder.getIsRecurring() &&
                reminder.getAttemptCount() < MAX_REMINDER_ATTEMPTS &&
                shouldSendRecurringReminder(reminder)) {

                sendReminder(reminder);
                sent++;
            }
        }

        log.info("Sent {} recurring payment reminders", sent);
    }

    /**
     * Mark reminders as completed when payment is received.
     */
    @Transactional
    public void markRemindersCompleted(Long installmentScheduleId) {
        log.info("Marking reminders as completed for installment schedule: {}", installmentScheduleId);

            List<PaymentReminder> activeReminders = reminderRepository
                .findByInstallmentScheduleIdAndStatusIn(installmentScheduleId,
                        List.of(ReminderStatus.PENDING, ReminderStatus.SENT));

        for (PaymentReminder reminder : activeReminders) {
            reminder.setStatus(ReminderStatus.COMPLETED);
            reminder.setAcknowledgedAt(LocalDateTime.now());
        }

        reminderRepository.saveAll(activeReminders);
        log.info("Marked {} reminders as completed", activeReminders.size());
    }

    /**
     * Create a reminder for a specific installment schedule.
     */
    private void createReminderForSchedule(InstallmentSchedule schedule, LocalDate reminderDate, LocalDate dueDate) {
        PaymentReminder reminder = new PaymentReminder();
        reminder.setInstallmentSchedule(schedule);
        reminder.setReminderDate(reminderDate);
        reminder.setDueDate(dueDate);
        reminder.setDaysBeforeDue(DEFAULT_REMINDER_DAYS);
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setIsRecurring(true);
        reminder.setAttemptCount(0);

        // Create personalized reminder message
        String message = createReminderMessage(schedule, dueDate);
        reminder.setReminderMessage(message);

        reminderRepository.save(reminder);
        log.debug("Created reminder for installment schedule: {}, due date: {}",
                schedule.getId(), dueDate);
    }

    /**
     * Send individual reminder (placeholder for actual implementation).
     */
    private void sendReminder(PaymentReminder reminder) {
        try {
            // TODO: Implement actual reminder sending (SMS, email, notification, etc.)
            log.info("Sending reminder ID: {} to customer for installment due: {}",
                    reminder.getId(), reminder.getDueDate());

            // Update reminder status and attempt count
            reminder.setStatus(ReminderStatus.SENT);
            reminder.setSentAt(LocalDateTime.now());
            reminder.setLastAttemptAt(LocalDateTime.now());
            reminder.setAttemptCount(reminder.getAttemptCount() + 1);

            reminderRepository.save(reminder);

        } catch (Exception e) {
            log.error("Failed to send reminder ID: {}", reminder.getId(), e);
            reminder.setLastAttemptAt(LocalDateTime.now());
            reminder.setAttemptCount(reminder.getAttemptCount() + 1);
            if (reminder.getAttemptCount() + 1 >= MAX_REMINDER_ATTEMPTS) {
                reminder.setStatus(ReminderStatus.FAILED);
            }
            reminderRepository.save(reminder);
        }
    }

    /**
     * Check if recurring reminder should be sent (avoid spamming).
     */
    private boolean shouldSendRecurringReminder(PaymentReminder reminder) {
        if (reminder.getLastAttemptAt() == null) {
            return true;
        }

        // Send recurring reminders every 2 days for overdue payments
        LocalDateTime lastAttempt = reminder.getLastAttemptAt();
        return lastAttempt.isBefore(LocalDateTime.now().minusDays(2));
    }

    /**
     * Create personalized reminder message.
     */
    private String createReminderMessage(InstallmentSchedule schedule, LocalDate dueDate) {

        String message= messageSource.getMessage(
                "payment.reminder.message",
                new Object[]{dueDate.toString(), schedule.getOriginalAmount()},
                LocaleContextHolder.getLocale()
        );
        return message;
    }
}
