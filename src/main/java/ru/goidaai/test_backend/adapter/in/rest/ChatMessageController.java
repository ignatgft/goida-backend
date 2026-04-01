package ru.goidaai.test_backend.adapter.in.rest;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.goidaai.test_backend.dto.ChatMessageDTO;
import ru.goidaai.test_backend.dto.ConversationDTO;
import ru.goidaai.test_backend.service.ChatMessageService;

@RestController
@RequestMapping("/api/messages")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @PostMapping
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @RequestParam("receiverId") String receiverId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "type", defaultValue = "TEXT") String type,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal Jwt jwt) {
        
        if (content == null || content.isBlank()) {
            content = "";
        }
        
        ChatMessageDTO message = chatMessageService.sendMessage(receiverId, content, type, file);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<Page<ChatMessageDTO>> getConversation(
            @PathVariable String otherUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {
        
        Page<ChatMessageDTO> messages = chatMessageService.getConversation(otherUserId, page, size);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<ChatMessageDTO>> getUnreadMessages(
            @AuthenticationPrincipal Jwt jwt) {
        
        List<ChatMessageDTO> messages = chatMessageService.getUnreadMessages();
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/read/{otherUserId}")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable String otherUserId,
            @AuthenticationPrincipal Jwt jwt) {
        
        chatMessageService.markMessagesAsRead(otherUserId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations(
            @AuthenticationPrincipal Jwt jwt) {
        
        List<ConversationDTO> conversations = chatMessageService.getConversations();
        return ResponseEntity.ok(conversations);
    }
}
