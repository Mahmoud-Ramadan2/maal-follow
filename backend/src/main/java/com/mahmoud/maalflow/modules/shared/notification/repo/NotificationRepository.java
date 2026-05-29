package com.mahmoud.maalflow.modules.shared.notification.repo;

import com.mahmoud.maalflow.modules.shared.notification.entity.Notification;
import com.mahmoud.maalflow.modules.shared.notification.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);
    List<Notification> findByCustomerIdAndStatus(Long customerId, NotificationStatus status);
    long countByCustomerIdAndStatus(Long customerId, NotificationStatus status);
}

