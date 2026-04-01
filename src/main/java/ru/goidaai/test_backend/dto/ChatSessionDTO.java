package ru.goidaai.test_backend.dto;

import java.time.Instant;
import java.util.List;

public class ChatSessionDTO {
    private String id;
    private String title;
    private String previewMessage;
    private String sessionType;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ChatMessageDTO> messages;

    public ChatSessionDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPreviewMessage() {
        return previewMessage;
    }

    public void setPreviewMessage(String previewMessage) {
        this.previewMessage = previewMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public List<ChatMessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessageDTO> messages) {
        this.messages = messages;
    }
}
