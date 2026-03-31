package ru.goidaai.test_backend.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.goidaai.test_backend.application.service.NotificationService;
import ru.goidaai.test_backend.domain.Notification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST контроллер для уведомлений
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(defaultValue = "0") int offset
    ) {
        String userId = jwt.getSubject();
        List<Notification> notifications = notificationService.getUserNotifications(userId, limit, offset);
        long unreadCount = notificationService.getUnreadCount(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications);
        response.put("unreadCount", unreadCount);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(jwt.getSubject()));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
        @AuthenticationPrincipal Jwt jwt
    ) {
        long count = notificationService.getUnreadCount(jwt.getSubject());
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
        @PathVariable String notificationId
    ) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
        @AuthenticationPrincipal Jwt jwt
    ) {
        notificationService.markAllAsRead(jwt.getSubject());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String notificationId
    ) {
        notificationService.deleteNotification(jwt.getSubject(), notificationId);
        return ResponseEntity.noContent().build();
    }
}
