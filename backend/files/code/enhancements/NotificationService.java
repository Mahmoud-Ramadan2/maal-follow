package com.mahmoud.maalflow.modules.shared.notification.service;

import com.mahmoud.maalflow.modules.shared.notification.entity.Notification;
import com.mahmoud.maalflow.modules.shared.notification.enums.NotificationPriority;
import com.mahmoud.maalflow.modules.shared.notification.enums.NotificationStatus;
import com.mahmoud.maalflow.modules.shared.notification.enums.NotificationType;
import com.mahmoud.maalflow.modules.shared.notification.repo.NotificationRepository;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Notification Service - manages in-app notifications.
 * Wire this into PaymentReminderService.sendReminder() to actually deliver notifications.
 *
 * Copy to: src/main/java/com/mahmoud/maalflow/modules/shared/notification/service/
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepo;
    private final UserRepository userRepository;

    /**
     * Create and persist a notification for a user.
     */
    @Transactional
    public Notification createNotification(Long userId, String title, String message,
                                           NotificationType type, NotificationPriority priority,
                                           String relatedEntityType, Long relatedEntityId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Cannot create notification: user {} not found", userId);
            return null;
        }

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .priority(priority)
                .status(NotificationStatus.UNREAD)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .build();

        Notification saved = notificationRepo.save(notification);
        log.info("Created notification {} for user {}: {}", saved.getId(), userId, title);
        return saved;
    }

    /**
     * Send a payment reminder notification (replaces the TODO stub in PaymentReminderService).
     */
    @Async
    @Transactional
    public void sendPaymentReminderNotification(Long userId, String customerName,
                                                 Long scheduleId, String dueDate, String amount) {
        String title = "تذكير بموعد القسط";
        String message = String.format("تذكير: قسط العميل %s بمبلغ %s مستحق بتاريخ %s",
                customerName, amount, dueDate);

        createNotification(userId, title, message,
                NotificationType.PAYMENT_REMINDER, NotificationPriority.HIGH,
                "INSTALLMENT_SCHEDULE", scheduleId);
    }

    /**
     * Get notifications for a user with pagination.
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(Long userId, int page, int size) {
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    /**
     * Get unread count for a user.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepo.countByUserIdAndStatus(userId, NotificationStatus.UNREAD);
    }

    /**
     * Mark a notification as read.
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepo.findById(notificationId).ifPresent(n -> {
            n.setStatus(NotificationStatus.READ);
            n.setReadAt(LocalDateTime.now());
            notificationRepo.save(n);
        });
    }

    /**
     * Mark all notifications as read for a user.
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepo.findByUserIdAndStatus(userId, NotificationStatus.UNREAD);
        for (Notification n : unread) {
            n.setStatus(NotificationStatus.READ);
            n.setReadAt(LocalDateTime.now());
        }
        notificationRepo.saveAll(unread);
        log.info("Marked {} notifications as read for user {}", unread.size(), userId);
    }
}

