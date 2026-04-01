package ru.goidaai.test_backend.adapter.in.rest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import ru.goidaai.test_backend.config.AppProperties;
import ru.goidaai.test_backend.dto.DocumentAnalysisResult;
import ru.goidaai.test_backend.dto.EnhancedChatRequest;
import ru.goidaai.test_backend.model.ChatHistory;
import ru.goidaai.test_backend.model.User;
import ru.goidaai.test_backend.repository.ChatHistoryRepository;
import ru.goidaai.test_backend.service.CurrentUserService;
import ru.goidaai.test_backend.service.GroqClient;

@RestController
@RequestMapping("/api/chat")
public class AiChatController {

    private final GroqClient groqClient;
    private final CurrentUserService currentUserService;
    private final ChatHistoryRepository chatHistoryRepository;
    private final String uploadDir;

    public AiChatController(
            GroqClient groqClient,
            CurrentUserService currentUserService,
            ChatHistoryRepository chatHistoryRepository,
            AppProperties appProperties) {
        this.groqClient = groqClient;
        this.currentUserService = currentUserService;
        this.chatHistoryRepository = chatHistoryRepository;
        this.uploadDir = appProperties.getStorage().getUploadDir();
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> chat(
            @RequestParam("message") String message,
            @RequestParam(value = "locale", defaultValue = "en") String locale,
            @RequestParam(value = "contextWindow", required = false) String contextWindow,
            @RequestParam(value = "chatId", required = false) String chatId,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal Jwt jwt) {
        
        User user = currentUserService.require(jwt.getSubject());
        String language = user.getLanguage() != null && !user.getLanguage().isBlank() 
                ? user.getLanguage() 
                : locale;

        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String imageUrl = saveFile(image, "chat_images");
                    imageUrls.add(imageUrl);
                }
            }
        }

        String systemPrompt = buildSystemPrompt(language, contextWindow);
        
        String response;
        if (!imageUrls.isEmpty()) {
            response = groqClient.chatWithImage(message, systemPrompt, language, imageUrls);
        } else {
            response = groqClient.chat(message, systemPrompt, language);
        }

        ChatHistory history = new ChatHistory();
        history.setUser(user);
        history.setMessage(message);
        history.setResponse(response);
        history.setProvider("groq");
        history.setModel(groqClient.getClass().getSimpleName());
        chatHistoryRepository.save(history);

        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("response", response);
        result.put("timestamp", Instant.now().toEpochMilli());
        result.put("chatId", chatId != null ? chatId : UUID.randomUUID().toString());
        result.put("hasImages", !imageUrls.isEmpty());
        result.put("imageUrls", imageUrls);

        DocumentAnalysisResult analysisResult = null;
        if (!imageUrls.isEmpty() && (message.toLowerCase().contains("analyze") || 
            message.toLowerCase().contains("проанализируй") ||
            message.toLowerCase().contains("расход") ||
            message.toLowerCase().contains("доход") ||
            message.toLowerCase().contains("expense") ||
            message.toLowerCase().contains("income"))) {
            analysisResult = groqClient.analyzeDocument(message, language, imageUrls, new ArrayList<>());
            result.put("analysis", analysisResult);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/analyze")
    public ResponseEntity<DocumentAnalysisResult> analyzeDocument(
            @RequestParam("message") String message,
            @RequestParam(value = "locale", defaultValue = "en") String locale,
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal Jwt jwt) {
        
        User user = currentUserService.require(jwt.getSubject());
        String language = user.getLanguage() != null && !user.getLanguage().isBlank() 
                ? user.getLanguage() 
                : locale;

        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                String imageUrl = saveFile(image, "chat_images");
                imageUrls.add(imageUrl);
            }
        }

        List<String> fileUrls = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileUrl = saveFile(file, "chat_files");
                    fileUrls.add(fileUrl);
                }
            }
        }

        DocumentAnalysisResult result = groqClient.analyzeDocument(message, language, imageUrls, fileUrls);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatHistory>> getChatHistory(
            @RequestParam(defaultValue = "50") int limit,
            @AuthenticationPrincipal Jwt jwt) {
        
        User user = currentUserService.require(jwt.getSubject());
        List<ChatHistory> history = chatHistoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        if (history.size() > limit) {
            history = history.subList(0, limit);
        }
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<ChatHistory> getChatHistoryItem(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
        
        User user = currentUserService.require(jwt.getSubject());
        return chatHistoryRepository.findByIdAndUserId(id, user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private String buildSystemPrompt(String language, String contextWindow) {
        boolean isRussian = "russian".equals(language) || "ru".equals(language);
        
        StringBuilder prompt = new StringBuilder();
        
        if (isRussian) {
            prompt.append("Ты опытный финансовый консультант и AI ассистент. ");
            prompt.append("Твоя задача - помогать пользователям управлять их финансами, анализировать расходы и доходы, давать рекомендации по бюджету, сбережениям и инвестициям. ");
            prompt.append("Отвечай профессионально, но дружелюбно. ");
            prompt.append("Если пользователь загружает изображения или файлы, анализируй их и извлекай финансовую информацию. ");
            prompt.append("Определяй, является ли документ расходом или доходом, и предлагай добавить его в систему учета. ");
        } else {
            prompt.append("You are an experienced financial consultant and AI assistant. ");
            prompt.append("Your task is to help users manage their finances, analyze expenses and income, provide budget, savings, and investment recommendations. ");
            prompt.append("Respond professionally but friendly. ");
            prompt.append("If the user uploads images or files, analyze them and extract financial information. ");
            prompt.append("Determine if the document is an expense or income, and suggest adding it to the accounting system. ");
        }
        
        if (contextWindow != null && !contextWindow.isBlank()) {
            if (isRussian) {
                prompt.append("\n\nКонтекст разговора: ").append(contextWindow);
            } else {
                prompt.append("\n\nConversation context: ").append(contextWindow);
            }
        }
        
        return prompt.toString();
    }

    private String saveFile(MultipartFile file, String folder) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path folderPath = Path.of(uploadDir, folder).toAbsolutePath().normalize();
            Files.createDirectories(folderPath);
            Path filePath = folderPath.resolve(fileName);
            
            Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            return "/uploads/" + folder + "/" + fileName;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save file", e);
        }
    }
}
