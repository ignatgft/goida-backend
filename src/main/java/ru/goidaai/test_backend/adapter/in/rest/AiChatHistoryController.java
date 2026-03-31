package ru.goidaai.test_backend.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.goidaai.test_backend.application.service.AiChatHistoryService;
import ru.goidaai.test_backend.domain.AiChatHistory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST контроллер для истории чатов с ИИ
 */
@RestController
@RequestMapping("/api/ai-chat")
@RequiredArgsConstructor
public class AiChatHistoryController {

    private final AiChatHistoryService aiChatHistoryService;

    @PostMapping("/chats")
    public ResponseEntity<AiChatHistory> createChat(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(required = false) String title
    ) {
        AiChatHistory history = aiChatHistoryService.createChat(jwt.getSubject(), title);
        return ResponseEntity.status(HttpStatus.CREATED).body(history);
    }

    @GetMapping("/chats")
    public ResponseEntity<List<AiChatHistory>> getUserChats(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(aiChatHistoryService.getUserChats(jwt.getSubject()));
    }

    @GetMapping("/chats/{historyId}")
    public ResponseEntity<AiChatHistory> getChatById(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String historyId
    ) {
        return ResponseEntity.ok(aiChatHistoryService.getChatById(jwt.getSubject(), historyId));
    }

    @PostMapping("/chats/{historyId}/messages")
    public ResponseEntity<AiChatHistory> addMessage(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String historyId,
        @RequestParam String role,
        @RequestParam String content,
        @RequestParam(required = false) String context
    ) {
        AiChatHistory history = aiChatHistoryService.addMessageToChat(
            jwt.getSubject(),
            historyId,
            role,
            content,
            context
        );
        return ResponseEntity.ok(history);
    }

    @PutMapping("/chats/{historyId}/title")
    public ResponseEntity<Void> updateChatTitle(
        @PathVariable String historyId,
        @RequestParam String title
    ) {
        aiChatHistoryService.updateChatTitle(historyId, title);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/chats/{historyId}")
    public ResponseEntity<Void> deleteChat(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String historyId
    ) {
        aiChatHistoryService.deleteChat(jwt.getSubject(), historyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/chats/{historyId}/clear")
    public ResponseEntity<Void> clearChatHistory(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String historyId
    ) {
        aiChatHistoryService.clearChatHistory(jwt.getSubject(), historyId);
        return ResponseEntity.ok().build();
    }
}
