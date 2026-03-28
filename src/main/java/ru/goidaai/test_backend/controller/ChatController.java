package ru.goidaai.test_backend.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.goidaai.test_backend.dto.ChatMessageDTO;
import ru.goidaai.test_backend.dto.ChatRequest;
import ru.goidaai.test_backend.service.ChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatMessageDTO chat(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChatRequest request) {
        String userId = jwt.getSubject();
        String userLocale = request.locale() != null ? request.locale() : jwt.getClaimAsString("locale");
        return chatService.chat(userId, request.message(), userLocale);
    }
}
