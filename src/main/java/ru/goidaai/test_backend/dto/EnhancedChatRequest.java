package ru.goidaai.test_backend.dto;

import java.util.List;

public class EnhancedChatRequest {

    private String message;
    private String locale;
    private String contextWindow;
    private List<String> imageUrls;
    private List<String> fileUrls;
    private String chatId;

    public EnhancedChatRequest() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getContextWindow() {
        return contextWindow;
    }

    public void setContextWindow(String contextWindow) {
        this.contextWindow = contextWindow;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<String> getFileUrls() {
        return fileUrls;
    }

    public void setFileUrls(List<String> fileUrls) {
        this.fileUrls = fileUrls;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}
