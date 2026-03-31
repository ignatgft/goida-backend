package ru.goidaai.test_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Доменная модель - Сообщение мессенджера
 */
@Getter
@Setter
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
    private Boolean isRead;
    private Instant createdAt;
    private Instant readAt;
}
