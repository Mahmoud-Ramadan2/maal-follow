package com.mahmoud.maalflow.modules.shared.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcher {

    private final List<NotificationSender> senders;

    public boolean dispatch(NotificationRequest request) {
        boolean delivered = false;

        for (NotificationSender sender : senders) {
            if (!sender.isEnabled()) {
                continue;
            }
            try {
                delivered = sender.send(request) || delivered;
            } catch (Exception ex) {
                log.error("Notification sender {} failed", sender.getClass().getSimpleName(), ex);
            }
        }

        return delivered;
    }
}

