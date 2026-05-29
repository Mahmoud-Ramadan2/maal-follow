package com.mahmoud.maalflow.modules.shared.notification.service;

public interface NotificationSender {

    boolean isEnabled();

    boolean send(NotificationRequest request);
}

