package ru.goidaai.test_backend.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.goidaai.test_backend.application.port.out.NotificationRepositoryPort;
import ru.goidaai.test_backend.domain.Notification;

import java.time.Instant;
import java.util.List;

/**
 * Сервис уведомлений
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepositoryPort notificationRepository;

    @Transactional
    public Notification createNotification(String userId, String title, String message, 
                                           Notification.NotificationType type) {
        Notification notification = Notification.builder()
            .userId(userId)
            .title(title)
            .message(message)
            .type(type)
            .isRead(false)
            .createdAt(Instant.now())
            .build();
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(String userId, int limit, int offset) {
        return notificationRepository.findByUserId(userId, limit, offset);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findUnreadByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public void markAsRead(String notificationId) {
        notificationRepository.markAsRead(notificationId, Instant.now());
    }

    @Transactional
    public void markAllAsRead(String userId) {
        List<Notification> unread = notificationRepository.findUnreadByUserId(userId);
        for (Notification notification : unread) {
            markAsRead(notification.getId());
        }
    }

    @Transactional
    public void deleteNotification(String userId, String notificationId) {
        notificationRepository.deleteByUserId(userId, notificationId);
    }

    // Методы для отправки специфичных уведомлений
    
    public void sendTransactionNotification(String userId, String transactionId, 
                                            String message, boolean isExpense) {
        createNotification(
            userId,
            isExpense ? "Новый расход" : "Новый доход",
            message,
            Notification.NotificationType.TRANSACTION
        );
    }

    public void sendAssetNotification(String userId, String assetName, String action) {
        createNotification(
            userId,
            "Изменение актива",
            action + ": " + assetName,
            Notification.NotificationType.ASSET
        );
    }

    public void sendReminderNotification(String userId, String reminderTitle) {
        createNotification(
            userId,
            "Напоминание",
            reminderTitle,
            Notification.NotificationType.REMINDER
        );
    }

    public void sendMessageNotification(String userId, String senderName, String messagePreview) {
        createNotification(
            userId,
            "Новое сообщение от " + senderName,
            messagePreview,
            Notification.NotificationType.MESSAGE
        );
    }
}
