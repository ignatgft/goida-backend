package ru.goidaai.test_backend.application.port.out;

import ru.goidaai.test_backend.domain.AiChatHistory;
import java.util.List;

/**
 * Порт репозитория для истории чатов с ИИ
 */
public interface AiChatHistoryRepositoryPort {
    AiChatHistory save(AiChatHistory history);
    List<AiChatHistory> findByUserId(String userId);
    AiChatHistory findById(String userId, String historyId);
    void deleteById(String userId, String historyId);
    void updateTitle(String historyId, String title);
}
