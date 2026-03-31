package ru.goidaai.test_backend.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.goidaai.test_backend.application.service.ChatService;
import ru.goidaai.test_backend.domain.ChatMessage;
import ru.goidaai.test_backend.domain.Conversation;
import ru.goidaai.test_backend.domain.MessageType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST + WebSocket контроллер для мессенджера
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/conversations/{recipientId}")
    public ResponseEntity<Map<String, String>> getOrCreateConversation(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String recipientId
    ) {
        Conversation conversation = chatService.getOrCreateConversation(jwt.getSubject(), recipientId);
        Map<String, String> response = new HashMap<>();
        response.put("conversationId", conversation.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<Conversation>> getUserConversations(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(chatService.getUserConversations(jwt.getSubject()));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ChatMessage> sendMessage(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String conversationId,
        @RequestParam String recipientId,
        @RequestParam String content,
        @RequestParam(defaultValue = "TEXT") String type
    ) {
        ChatMessage message = chatService.sendMessage(
            conversationId, 
            jwt.getSubject(), 
            recipientId, 
            content, 
            MessageType.valueOf(type.toUpperCase())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(
        @PathVariable String conversationId,
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(defaultValue = "0") int offset
    ) {
        return ResponseEntity.ok(chatService.getMessages(conversationId, limit, offset));
    }

    @GetMapping("/messages/unread")
    public ResponseEntity<List<ChatMessage>> getUnreadMessages(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(chatService.getUnreadMessages(jwt.getSubject()));
    }

    @PostMapping("/messages/{messageId}/read")
    public ResponseEntity<Void> markMessageAsRead(@PathVariable String messageId) {
        chatService.markMessageAsRead(messageId);
        return ResponseEntity.ok().build();
    }

    /**
     * WebSocket endpoint для отправки сообщений в реальном времени
     */
    @MessageMapping("/chat.send")
    @SendTo("/topic/messages")
    public ChatMessage sendViaWebSocket(@Payload ChatMessage message) {
        return message;
    }

    /**
     * WebSocket endpoint для подписки на сообщения конверсации
     */
    @SubscribeMapping("/topic/conversations/{conversationId}")
    public void subscribeToConversation(@PathVariable String conversationId) {
        // Подписка обрабатывается автоматически через STOMP
    }
}
