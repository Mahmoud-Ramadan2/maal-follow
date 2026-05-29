package com.mahmoud.maalflow.modules.shared.notification.service;

import com.mahmoud.maalflow.modules.shared.notification.entity.Notification;
import com.mahmoud.maalflow.modules.shared.notification.enums.NotificationPriority;
import com.mahmoud.maalflow.modules.shared.notification.enums.NotificationStatus;
import com.mahmoud.maalflow.modules.shared.notification.enums.NotificationType;
import com.mahmoud.maalflow.modules.shared.notification.repo.NotificationRepository;
import com.mahmoud.maalflow.modules.installments.customer.entity.Customer;
import com.mahmoud.maalflow.modules.installments.customer.repo.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Notification Service - manages in-app notifications.
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepo;
    private final CustomerRepository customerRepository;

    /**
     * Create and persist a notification for a customer.
     */
    @Transactional
    public Notification createNotification(Long customerId, String title, String message,
                                           NotificationType type, NotificationPriority priority,
                                           String relatedEntityType, Long relatedEntityId) {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) {
            log.warn("Cannot create notification: customer {} not found", customerId);
            return null;
        }

        Notification notification = Notification.builder()
                .customer(customer)
                .title(title)
                .message(message)
                .type(type)
                .priority(priority)
                .status(NotificationStatus.UNREAD)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .build();

        Notification saved = notificationRepo.save(notification);
        log.info("Created notification {} for customer {}: {}", saved.getId(), customerId, title);
        return saved;
    }


    /**
     * Get notifications for a customer with pagination.
     */
    @Transactional(readOnly = true)
    public Page<Notification> getCustomerNotifications(Long customerId, int page, int size) {
        return notificationRepo.findByCustomerIdOrderByCreatedAtDesc(customerId, PageRequest.of(page, size));
    }

    /**
     * Get unread count for a customer.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long customerId) {
        return notificationRepo.countByCustomerIdAndStatus(customerId, NotificationStatus.UNREAD);
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
     * Mark all notifications as read for a customer.
     */
    @Transactional
    public void markAllAsRead(Long customerId) {
        List<Notification> unread = notificationRepo.findByCustomerIdAndStatus(customerId, NotificationStatus.UNREAD);
        for (Notification n : unread) {
            n.setStatus(NotificationStatus.READ);
            n.setReadAt(LocalDateTime.now());
        }
        notificationRepo.saveAll(unread);
        log.info("Marked {} notifications as read for customer {}", unread.size(), customerId);
    }
}
