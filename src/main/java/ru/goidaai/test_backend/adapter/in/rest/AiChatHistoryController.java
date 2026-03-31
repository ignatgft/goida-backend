package ru.goidaai.test_backend.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.goidaai.test_backend.application.service.AiChatHistoryService;
import ru.goidaai.test_backend.domain.AiChatHistory;
import ru.goidaai.test_backend.service.DocumentAnalysisService;

import java.io.IOException;
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
    private final DocumentAnalysisService documentAnalysisService;

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

    /**
     * Отправка сообщения с вложением (PDF, изображение)
     */
    @PostMapping(value = "/chats/{historyId}/messages-with-file", 
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> sendMessageWithFile(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String historyId,
        @RequestParam("file") MultipartFile file,
        @RequestParam(required = false) String message
    ) {
        try {
            // Анализируем файл
            var analysis = documentAnalysisService.analyzeDocument(file);

            // Добавляем сообщение с контекстом анализа
            String context = String.format("""
                Пользователь загрузил документ:
                - Тип: %s
                - Файл: %s
                - Сумма: %s %s
                - Дата: %s
                - Мерчант: %s
                
                Распознанный текст:
                %s
                """,
                analysis.documentType(),
                analysis.metadata().fileName(),
                analysis.totalAmount(),
                analysis.currency(),
                analysis.documentDate(),
                analysis.merchantName(),
                analysis.rawText()
            );

            aiChatHistoryService.addMessageToChat(
                jwt.getSubject(),
                historyId,
                "user",
                message != null ? message : "Проанализируй этот документ",
                context
            );

            // Формируем ответ
            Map<String, Object> response = new HashMap<>();
            response.put("analysis", analysis);
            response.put("message", "Документ проанализирован и добавлен в чат");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при обработке файла: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
