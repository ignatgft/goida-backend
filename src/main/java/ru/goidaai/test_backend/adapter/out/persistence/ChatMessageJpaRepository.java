package ru.goidaai.test_backend.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ChatMessageJpaRepository implements ChatMessageRepositoryPort {

    private final ChatMessageEntityRepository repository;

    @Override
    public ChatMessage save(ChatMessage chatMessage) {
        ChatMessageEntity entity = ChatMessageEntity.fromDomain(chatMessage);
        ChatMessageEntity saved = repository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<ChatMessage> findById(String id) {
        return repository.findById(id).map(ChatMessageEntity::toDomain);
    }

    @Override
    public List<ChatMessage> findByConversationId(String conversationId, int limit, int offset) {
        return repository.findByConversationIdOrderByCreatedAtDesc(conversationId, limit, offset).stream()
            .map(ChatMessageEntity::toDomain)
            .toList();
    }

    public List<ChatMessage> findUnreadMessages(String recipientId) {
        return repository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(recipientId).stream()
            .map(ChatMessageEntity::toDomain)
            .toList();
    }

    @Modifying
    @Query("UPDATE ChatMessageEntity m SET m.isRead = true, m.readAt = :readAt WHERE m.id = :messageId")
    public void markAsRead(String messageId, Instant readAt) {
        repository.markAsReadInternal(messageId, readAt);
    }

    public void markAsRead(String messageId) {
        markAsRead(messageId, Instant.now());
    }
}

/**
 * Внутренний интерфейс для Spring Data JPA
 */
interface ChatMessageEntityRepository extends org.springframework.data.jpa.repository.JpaRepository<ChatMessageEntity, String> {
    List<ChatMessageEntity> findByConversationIdOrderByCreatedAtDesc(String conversationId, int limit, int offset);
    List<ChatMessageEntity> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(String recipientId);
    
    @Modifying
    @Query("UPDATE ChatMessageEntity m SET m.isRead = true, m.readAt = :readAt WHERE m.id = :messageId")
    void markAsReadInternal(@Param("messageId") String messageId, @Param("readAt") Instant readAt);
}
