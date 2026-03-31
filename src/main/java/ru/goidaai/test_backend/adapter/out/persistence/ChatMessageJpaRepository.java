package ru.goidaai.test_backend.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.goidaai.test_backend.application.port.out.ChatMessageRepositoryPort;
import ru.goidaai.test_backend.domain.ChatMessage;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Адаптер репозитория для ChatMessage
 */
@Repository
public interface ChatMessageJpaRepository extends JpaRepository<ChatMessageEntity, String>, ChatMessageRepositoryPort {

    @Override
    default ChatMessage save(ChatMessage chatMessage) {
        ChatMessageEntity entity = ChatMessageEntity.fromDomain(chatMessage);
        ChatMessageEntity saved = super.save(entity);
        return saved.toDomain();
    }

    @Override
    default Optional<ChatMessage> findById(String id) {
        return super.findById(id).map(ChatMessageEntity::toDomain);
    }

    @Override
    @Query("SELECT m FROM ChatMessageEntity m WHERE m.conversationId = :conversationId ORDER BY m.createdAt DESC")
    List<ChatMessage> findByConversationId(String conversationId, @Param("limit") int limit, @Param("offset") int offset);

    @Override
    @Query("SELECT m FROM ChatMessageEntity m WHERE m.recipientId = :recipientId AND m.isRead = false ORDER BY m.createdAt DESC")
    List<ChatMessage> findUnreadMessages(String recipientId);

    @Override
    @Modifying
    @Query("UPDATE ChatMessageEntity m SET m.isRead = true, m.readAt = :readAt WHERE m.id = :messageId")
    void markAsRead(@Param("messageId") String messageId, @Param("readAt") Instant readAt);

    @Override
    default void markAsRead(String messageId) {
        markAsRead(messageId, Instant.now());
    }
}
