package ru.goidaai.test_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Доменная модель - Конверсация (чат между пользователями)
 */
@Getter
@Setter
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
