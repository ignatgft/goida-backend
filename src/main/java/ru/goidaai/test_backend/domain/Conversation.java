package ru.goidaai.test_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Доменная модель - Конверсация (чат между пользователями)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    private String id;
    private String user1Id;
    private String user2Id;
    private String lastMessage;
    private Instant lastMessageAt;
    private Integer unreadCount;
    private Instant createdAt;
    private Instant updatedAt;
}
