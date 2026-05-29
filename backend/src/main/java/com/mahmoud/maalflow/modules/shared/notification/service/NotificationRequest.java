package com.mahmoud.maalflow.modules.shared.notification.service;

import com.mahmoud.maalflow.modules.shared.notification.enums.NotificationPriority;
import com.mahmoud.maalflow.modules.shared.notification.enums.NotificationType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationRequest {

    private final Long customerId;
    private final String title;
    private final String message;
    private final NotificationType type;
    private final NotificationPriority priority;
    private final String relatedEntityType;
    private final Long relatedEntityId;
    private final String recipientEmail;
    private final String recipientPhone;
    private final String recipientName;
}

