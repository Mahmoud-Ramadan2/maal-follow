package com.mahmoud.maalflow.modules.shared.notification.repo;

import com.mahmoud.maalflow.modules.shared.notification.entity.Notification;
import com.mahmoud.maalflow.modules.shared.notification.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Copy to: src/main/java/com/mahmoud/maalflow/modules/shared/notification/repo/
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status);
    long countByUserIdAndStatus(Long userId, NotificationStatus status);
}

