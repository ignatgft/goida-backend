package ru.goidaai.test_backend.adapter.in.rest;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.goidaai.test_backend.dto.ChatSessionDTO;
import ru.goidaai.test_backend.dto.CreateChatSessionRequest;
import ru.goidaai.test_backend.dto.UpdateChatSessionRequest;
import ru.goidaai.test_backend.service.ChatSessionService;

@RestController
@RequestMapping("/api/chat-sessions")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    public ChatSessionController(ChatSessionService chatSessionService) {
        this.chatSessionService = chatSessionService;
    }

    @GetMapping
    public ResponseEntity<List<ChatSessionDTO>> getAllSessions(
            @AuthenticationPrincipal Jwt jwt) {
        List<ChatSessionDTO> sessions = chatSessionService.getAllSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatSessionDTO> getSession(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
        ChatSessionDTO session = chatSessionService.getSessionById(id);
        return ResponseEntity.ok(session);
    }

    @PostMapping
    public ResponseEntity<ChatSessionDTO> createSession(
            @RequestBody(required = false) CreateChatSessionRequest request,
            @RequestParam(required = false, defaultValue = "MESSENGER") String type,
            @AuthenticationPrincipal Jwt jwt) {
        String title = request != null ? request.getTitle() : null;
        ChatSessionDTO session = chatSessionService.createSession(title, type);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/ai")
    public ResponseEntity<ChatSessionDTO> createAISession(
            @RequestBody(required = false) CreateChatSessionRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String title = request != null ? request.getTitle() : "New AI Chat";
        ChatSessionDTO session = chatSessionService.createSession(title, "AI");
        return ResponseEntity.ok(session);
    }

    @GetMapping("/ai")
    public ResponseEntity<List<ChatSessionDTO>> getAISessions(
            @AuthenticationPrincipal Jwt jwt) {
        List<ChatSessionDTO> sessions = chatSessionService.getAllSessions()
                .stream()
                .filter(s -> "AI".equals(s.getSessionType()))
                .toList();
        return ResponseEntity.ok(sessions);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ChatSessionDTO> updateSession(
            @PathVariable String id,
            @RequestBody UpdateChatSessionRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        ChatSessionDTO session = chatSessionService.updateSessionTitle(id, request.getTitle());
        return ResponseEntity.ok(session);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
        chatSessionService.deleteSession(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ChatSessionDTO> addMessageToSession(
            @PathVariable String id,
            @RequestBody Map<String, String> messageData,
            @AuthenticationPrincipal Jwt jwt) {
        String content = messageData.get("content");
        String preview = content != null && content.length() > 100 
                ? content.substring(0, 100) + "..." 
                : content;
        ChatSessionDTO session = chatSessionService.updateSessionPreview(id, preview);
        return ResponseEntity.ok(session);
    }
}
