package ru.goidaai.test_backend.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.goidaai.test_backend.application.port.out.AiChatHistoryRepositoryPort;
import ru.goidaai.test_backend.domain.AiChatHistory;

import java.time.Instant;
import java.util.List;

/**
 * Сервис истории чатов с ИИ
 */
@Service
@RequiredArgsConstructor
public class AiChatHistoryService {

    private final AiChatHistoryRepositoryPort aiChatHistoryRepository;

    @Transactional
    public AiChatHistory createChat(String userId, String title) {
        AiChatHistory history = AiChatHistory.builder()
            .userId(userId)
            .title(title != null ? title : "Новый чат")
            .messages(List.of())
            .messageCount(0)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        return aiChatHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<AiChatHistory> getUserChats(String userId) {
        return aiChatHistoryRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public AiChatHistory getChatById(String userId, String historyId) {
        return aiChatHistoryRepository.findById(userId, historyId);
    }

    @Transactional
    public AiChatHistory addMessageToChat(String userId, String historyId,
                                           String role, String content, String context) {
        AiChatHistory history = aiChatHistoryRepository.findById(userId, historyId);
        if (history == null) {
            throw new RuntimeException("Chat history not found");
        }

        AiChatHistory.AiMessage message = AiChatHistory.AiMessage.builder()
            .role(role)
            .content(content)
            .context(context)
            .timestamp(Instant.now())
            .build();

        List<AiChatHistory.AiMessage> updatedMessages = new java.util.ArrayList<>(history.getMessages());
        updatedMessages.add(message);

        history.setMessages(updatedMessages);
        history.setMessageCount(history.getMessageCount() + 1);
        history.setUpdatedAt(Instant.now());

        return aiChatHistoryRepository.save(history);
    }

    @Transactional
    public void updateChatTitle(String historyId, String title) {
        aiChatHistoryRepository.updateTitle(historyId, title);
    }

    @Transactional
    public void deleteChat(String userId, String historyId) {
        aiChatHistoryRepository.deleteById(userId, historyId);
    }

    @Transactional
    public void clearChatHistory(String userId, String historyId) {
        AiChatHistory history = aiChatHistoryRepository.findById(userId, historyId);
        if (history != null) {
            history.setMessages(List.of());
            history.setMessageCount(0);
            history.setUpdatedAt(Instant.now());
            aiChatHistoryRepository.save(history);
        }
    }
}
