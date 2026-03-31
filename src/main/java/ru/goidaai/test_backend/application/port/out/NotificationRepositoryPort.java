package ru.goidaai.test_backend.application.port.out;

import ru.goidaai.test_backend.domain.Notification;
import java.time.Instant;
import java.util.List;

/**
 * Порт репозитория для уведомлений
 */
public interface NotificationRepositoryPort {
    Notification save(Notification notification);
    List<Notification> findByUserId(String userId, int limit, int offset);
    List<Notification> findUnreadByUserId(String userId);
    void markAsRead(String notificationId, Instant readAt);
    void deleteByUserId(String userId, String notificationId);
    long countUnreadByUserId(String userId);
}
