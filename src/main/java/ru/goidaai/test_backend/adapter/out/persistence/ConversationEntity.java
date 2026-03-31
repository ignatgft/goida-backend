package ru.goidaai.test_backend.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.goidaai.test_backend.domain.Conversation;

import java.time.Instant;

/**
 * JPA сущность для Conversation
 */
@Entity
@Table(name = "conversations", indexes = {
    @Index(name = "idx_conversations_user1_id", columnList = "user1_id"),
    @Index(name = "idx_conversations_user2_id", columnList = "user2_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, name = "user1_id")
    private String user1Id;

    @Column(nullable = false, name = "user2_id")
    private String user2Id;

    @Column(name = "last_message", length = 500)
    private String lastMessage;

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    @Column(name = "unread_count")
    private Integer unreadCount;

    @Column(nullable = false, name = "created_at")
    private Instant createdAt;

    @Column(nullable = false, name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (unreadCount == null) unreadCount = 0;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public Conversation toDomain() {
        return Conversation.builder()
            .id(id)
            .user1Id(user1Id)
            .user2Id(user2Id)
            .lastMessage(lastMessage)
            .lastMessageAt(lastMessageAt)
            .unreadCount(unreadCount)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }

    public static ConversationEntity fromDomain(Conversation conversation) {
        return ConversationEntity.builder()
            .id(conversation.getId())
            .user1Id(conversation.getUser1Id())
            .user2Id(conversation.getUser2Id())
            .lastMessage(conversation.getLastMessage())
            .lastMessageAt(conversation.getLastMessageAt())
            .unreadCount(conversation.getUnreadCount())
            .createdAt(conversation.getCreatedAt())
            .updatedAt(conversation.getUpdatedAt())
            .build();
    }
}
