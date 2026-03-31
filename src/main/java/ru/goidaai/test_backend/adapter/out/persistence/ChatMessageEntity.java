package ru.goidaai.test_backend.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.goidaai.test_backend.domain.ChatMessage;
import ru.goidaai.test_backend.domain.MessageType;

import java.time.Instant;

/**
 * JPA сущность для ChatMessage
 */
@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_chat_messages_conversation_id", columnList = "conversation_id"),
    @Index(name = "idx_chat_messages_recipient_id", columnList = "recipient_id"),
    @Index(name = "idx_chat_messages_is_read", columnList = "is_read")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, name = "conversation_id")
    private String conversationId;

    @Column(nullable = false, name = "sender_id")
    private String senderId;

    @Column(nullable = false, name = "recipient_id")
    private String recipientId;

    @Column(nullable = false, length = 5000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MessageType type;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(nullable = false, name = "created_at")
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        if (isRead == null) isRead = false;
        if (type == null) type = MessageType.TEXT;
    }

    public ChatMessage toDomain() {
        return ChatMessage.builder()
            .id(id)
            .conversationId(conversationId)
            .senderId(senderId)
            .recipientId(recipientId)
            .content(content)
            .type(type)
            .isRead(isRead)
            .createdAt(createdAt)
            .readAt(readAt)
            .build();
    }

    public static ChatMessageEntity fromDomain(ChatMessage message) {
        return ChatMessageEntity.builder()
            .id(message.getId())
            .conversationId(message.getConversationId())
            .senderId(message.getSenderId())
            .recipientId(message.getRecipientId())
            .content(message.getContent())
            .type(message.getType())
            .isRead(message.getIsRead())
            .createdAt(message.getCreatedAt())
            .readAt(message.getReadAt())
            .build();
    }
}
