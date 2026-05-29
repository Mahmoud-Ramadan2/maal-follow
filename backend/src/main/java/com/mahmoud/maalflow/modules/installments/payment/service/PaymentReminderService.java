package com.mahmoud.maalflow.modules.installments.payment.service;

import com.mahmoud.maalflow.modules.installments.payment.dto.ReminderRecipient;
import com.mahmoud.maalflow.modules.installments.schedule.entity.InstallmentSchedule;
import com.mahmoud.maalflow.modules.installments.schedule.repo.InstallmentScheduleRepository;
import com.mahmoud.maalflow.modules.installments.payment.entity.PaymentReminder;
import com.mahmoud.maalflow.modules.installments.payment.enums.ReminderStatus;
import com.mahmoud.maalflow.modules.installments.payment.repo.PaymentReminderRepository;
import com.mahmoud.maalflow.modules.shared.notification.enums.NotificationPriority;
import com.mahmoud.maalflow.modules.shared.notification.enums.NotificationType;
import com.mahmoud.maalflow.modules.shared.notification.service.NotificationDispatcher;
import com.mahmoud.maalflow.modules.shared.notification.service.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    private static final String RELATED_ENTITY_TYPE = "INSTALLMENT_SCHEDULE";

    private final PaymentReminderRepository reminderRepository;
    private final InstallmentScheduleRepository scheduleRepository;
    private final PaymentReminderMessageBuilder messageBuilder;
    private final ReminderRecipientResolver recipientResolver;
    private final NotificationDispatcher notificationDispatcher;



    /**
     * Create payment reminders for upcoming due dates.
     * Called automatically to generate reminders 5 days before payment due.
     */
    @Scheduled(cron = "0 00 08 * * ?") // Daily at 8 AM
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
                    .existsByInstallmentSchedule(schedule);

            if (!reminderExists) {
                createReminderForSchedule(schedule, today);
                created++;
            }
        }

        log.info("Created {} new payment reminders", created);
    }

    //
public void changeRecurringState(boolean isRecurring, Long installmentScheduleId) {
        List<PaymentReminder> reminders = reminderRepository
                .findByInstallmentScheduleId(installmentScheduleId);

        for (PaymentReminder reminder : reminders) {
            reminder.setIsRecurring(isRecurring);
        }

        reminderRepository.saveAll(reminders);
        log.info("Updated recurring state to {} for {} reminders of installment schedule ID: {}",
                isRecurring, reminders.size(), installmentScheduleId);
    }




/**
     * Send pending reminders (scheduled job).
* Implements: “Make a reminder to submit the claim five days before it is paid.”
     * */

    @Scheduled(cron = "0 30 08 * * ?") // Daily at 8:30 AM
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
    @Scheduled(cron = "0 00 09 * * ?") // Daily at 9AM
    @Transactional
    public void sendRecurringReminders() {
        log.info("Sending recurring reminders for overdue payments");

        LocalDate today = LocalDate.now();
        LocalDateTime retryBefore = today.minusDays(2).atStartOfDay();
        // retrun only with state SENT and send before 2 days
        List<PaymentReminder> overdueReminders = reminderRepository
                .findOverdueRemindersAndRecurringTrue(today, retryBefore);

        int sent = 0;
        for (PaymentReminder reminder : overdueReminders) {
            if (reminder.getAttemptCount() < MAX_REMINDER_ATTEMPTS &&
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
                .findByInstallmentScheduleIdAndStatusInAndIsRecurringTrue(installmentScheduleId,
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
    private void createReminderForSchedule(InstallmentSchedule schedule, LocalDate today) {
        PaymentReminder reminder = new PaymentReminder();
        reminder.setInstallmentSchedule(schedule);
        reminder.setReminderDate(today);
        reminder.setDueDate(schedule.getDueDate());
        reminder.setDaysBeforeDue(DEFAULT_REMINDER_DAYS);
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setIsRecurring(true);
        reminder.setAttemptCount(0);

        String message = messageBuilder.buildMessage(schedule);
        reminder.setReminderMessage(message);

        reminderRepository.save(reminder);
        log.debug("Created reminder for installment schedule: {}, due date: {}",
                schedule.getId(), schedule.getDueDate());
    }

    /**
     * Send individual reminder using configured notification senders.
     */
    private void sendReminder(PaymentReminder reminder) {
        try {
            InstallmentSchedule schedule = reminder.getInstallmentSchedule();
            Optional<ReminderRecipient> recipient = recipientResolver.resolve(schedule);
            if (recipient.isEmpty()) {
                log.warn("No reminder recipient found for schedule {}", schedule.getId());
                recordSendFailure(reminder);
                return;
            }

            ReminderRecipient resolved = recipient.get();
            NotificationRequest request = NotificationRequest.builder()
                    .customerId(resolved.customerId())
                    .title(messageBuilder.buildTitle())
                    .message(reminder.getReminderMessage())
                    .type(NotificationType.PAYMENT_REMINDER)
                    .priority(NotificationPriority.HIGH)
                    .relatedEntityType(RELATED_ENTITY_TYPE)
                    .relatedEntityId(schedule.getId())
                    .recipientEmail(resolved.email())
                    .recipientPhone(resolved.phone())
                    .recipientName(resolved.name())
                    .build();

            boolean delivered = notificationDispatcher.dispatch(request);
            if (delivered) {
                markReminderAsSent(reminder);
            } else {
                recordSendFailure(reminder);
            }
        } catch (Exception e) {
            log.error("Failed to send reminder ID: {}", reminder.getId(), e);
            recordSendFailure(reminder);
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

    private void markReminderAsSent(PaymentReminder reminder) {
        LocalDateTime now = LocalDateTime.now();

        reminder.setStatus(ReminderStatus.SENT);
        reminder.setSentAt(now);
        reminder.setLastAttemptAt(now);
        reminder.setAttemptCount(reminder.getAttemptCount() + 1);

        reminderRepository.save(reminder);
    }

    private void recordSendFailure(PaymentReminder reminder) {
        reminder.setLastAttemptAt(LocalDateTime.now());
        reminder.setAttemptCount(reminder.getAttemptCount() + 1);

        if (reminder.getAttemptCount() >= MAX_REMINDER_ATTEMPTS) {
            reminder.setStatus(ReminderStatus.FAILED);
        }

        reminderRepository.save(reminder);
    }
}


