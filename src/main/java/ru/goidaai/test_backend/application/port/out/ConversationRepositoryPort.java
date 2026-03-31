package ru.goidaai.test_backend.application.port.out;

import ru.goidaai.test_backend.domain.Conversation;
import java.util.List;
import java.util.Optional;

/**
 * Порт репозитория для Conversation
 */
public interface ConversationRepositoryPort {
    Conversation save(Conversation conversation);
    Optional<Conversation> findById(String id);
    Optional<Conversation> findByUser1IdAndUser2Id(String user1Id, String user2Id);
    List<Conversation> findByUserId(String userId);
    void deleteById(String id);
}
