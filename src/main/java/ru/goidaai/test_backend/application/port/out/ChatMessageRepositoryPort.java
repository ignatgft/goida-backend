package ru.goidaai.test_backend.application.port.out;

import ru.goidaai.test_backend.domain.ChatMessage;
import java.util.List;
import java.util.Optional;

/**
 * Порт репозитория для ChatMessage
 */
public interface ChatMessageRepositoryPort {
    ChatMessage save(ChatMessage chatMessage);
    Optional<ChatMessage> findById(String id);
    List<ChatMessage> findByConversationId(String conversationId, int limit, int offset);
    List<ChatMessage> findUnreadMessages(String recipientId);
    void markAsRead(String messageId);
}
