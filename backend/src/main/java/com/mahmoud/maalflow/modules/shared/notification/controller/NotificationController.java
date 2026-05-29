package com.mahmoud.maalflow.modules.shared.notification.controller;

import com.mahmoud.maalflow.modules.shared.notification.entity.Notification;
import com.mahmoud.maalflow.modules.shared.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Notification REST Controller.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<Notification>> getCustomerNotifications(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getCustomerNotifications(customerId, page, size));
    }

    @GetMapping("/customer/{customerId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Long customerId) {
        return ResponseEntity.ok(Map.of("unreadCount", service.getUnreadCount(customerId)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        service.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/customer/{customerId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long customerId) {
        service.markAllAsRead(customerId);
        return ResponseEntity.ok().build();
    }
}

