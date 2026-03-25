
package com.mahmoud.maalflow.modules.shared.notification.entity;

import com.mahmoud.maalflow.modules.shared.notification.enums.NotificationPriority;
import com.mahmoud.maalflow.modules.shared.notification.enums.NotificationStatus;
import com.mahmoud.maalflow.modules.shared.notification.enums.NotificationType;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Notification entity for alerts and reminders.
 *
 * @author Mahmoud
 */
@Entity
@Table(name = "notification", indexes = {
        @Index(name = "idx_notification_user", columnList = "user_id"),
        @Index(name = "idx_notification_status", columnList = "status"),
        @Index(name = "idx_notification_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private NotificationPriority priority = NotificationPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private NotificationStatus status = NotificationStatus.UNREAD;

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", status=" + status +
                '}';
    }
}

