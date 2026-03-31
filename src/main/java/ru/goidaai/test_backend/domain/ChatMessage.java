package ru.goidaai.test_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Доменная модель - Сообщение мессенджера
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String id;
    private String conversationId;
    private String senderId;
    private String recipientId;
    private String content;
    private MessageType type;
    Boolean isRead;
    private Instant createdAt;
    private Instant readAt;
}
