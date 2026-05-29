package com.mahmoud.maalflow.modules.shared.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsNotificationSenderStub implements NotificationSender {

    @Value("${notification.sms.enabled:false}")
    private boolean enabled;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean send(NotificationRequest request) {
        if (request.getRecipientPhone() == null || request.getRecipientPhone().isBlank()) {
            log.warn("Skipping SMS notification: missing recipient phone");
            return false;
        }

        log.info("SMS sender stub disabled; would send to {}", request.getRecipientPhone());
        return false;
    }
}
