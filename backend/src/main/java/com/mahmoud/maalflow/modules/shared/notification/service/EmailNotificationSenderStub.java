package com.mahmoud.maalflow.modules.shared.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailNotificationSenderStub implements NotificationSender {

    @Value("${notification.email.enabled:false}")
    private boolean enabled;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean send(NotificationRequest request) {
        if (request.getRecipientEmail() == null || request.getRecipientEmail().isBlank()) {
            log.warn("Skipping email notification: missing recipient email");
            return false;
        }

        log.info("Email sender stub disabled; would send to {}", request.getRecipientEmail());
        return false;
    }
}
