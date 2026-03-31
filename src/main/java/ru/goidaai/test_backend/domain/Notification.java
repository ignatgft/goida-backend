package ru.goidaai.test_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Доменная модель - Уведомление пользователя
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private String id;
    private String userId;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean isRead;
    private Instant createdAt;
    private Instant readAt;
    
    public enum NotificationType {
        TRANSACTION,
        ASSET,
        REMINDER,
        SYSTEM,
        MESSAGE
    }
}
