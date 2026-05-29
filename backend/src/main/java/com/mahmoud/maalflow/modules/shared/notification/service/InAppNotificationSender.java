package com.mahmoud.maalflow.modules.shared.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationSender implements NotificationSender {

    private final NotificationService notificationService;

    @Value("${notification.inapp.enabled:true}")
    private boolean enabled;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean send(NotificationRequest request) {
        if (request.getCustomerId() == null) {
            log.warn("Skipping in-app notification: missing customerId");
            return false;
        }

        return notificationService.createNotification(
                request.getCustomerId(),
                request.getTitle(),
                request.getMessage(),
                request.getType(),
                request.getPriority(),
                request.getRelatedEntityType(),
                request.getRelatedEntityId()
        ) != null;
    }
}
