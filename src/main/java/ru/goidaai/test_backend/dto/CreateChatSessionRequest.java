package ru.goidaai.test_backend.dto;

public class CreateChatSessionRequest {
    private String title;

    public CreateChatSessionRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
