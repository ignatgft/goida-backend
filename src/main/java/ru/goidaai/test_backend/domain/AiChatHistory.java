package ru.goidaai.test_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

/**
 * Доменная модель - История чата с ИИ
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatHistory {
    private String id;
    private String userId;
    private String title;
    private List<AiMessage> messages;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer messageCount;
    
    /**
     * Отдельное сообщение в истории чата
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiMessage {
        private String role; // user, assistant, system
        private String content;
        private Instant timestamp;
        private String context; // контекст для этого сообщения
    }
}
