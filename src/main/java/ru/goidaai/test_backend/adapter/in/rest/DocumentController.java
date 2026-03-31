package ru.goidaai.test_backend.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.goidaai.test_backend.dto.DocumentAnalysisResult;
import ru.goidaai.test_backend.service.DocumentAnalysisService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * REST контроллер для анализа документов (чеки, квитанции, скриншоты)
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentAnalysisService documentAnalysisService;

    /**
     * Загрузка и анализ документа
     * Поддерживаются: PDF, PNG, JPG, JPEG
     */
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentAnalysisResult> analyzeDocument(
        @RequestParam("file") MultipartFile file,
        @AuthenticationPrincipal Jwt jwt
    ) {
        try {
            // Проверка типа файла
            String contentType = file.getContentType();
            if (contentType == null) {
                return ResponseEntity.badRequest().build();
            }

            boolean isSupported = contentType.contains("pdf") || 
                                  contentType.contains("image");
            if (!isSupported) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Неподдерживаемый тип файла. Разрешены: PDF, PNG, JPG, JPEG");
                return ResponseEntity.badRequest().body(null);
            }

            // Проверка размера (макс 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Файл слишком большой. Максимум 10MB");
                return ResponseEntity.badRequest().body(null);
            }

            // Анализ документа
            DocumentAnalysisResult result = documentAnalysisService.analyzeDocument(file);

            return ResponseEntity.ok(result);

        } catch (IOException e) {
            log.error("Ошибка при анализе документа: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при обработке файла: " + e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * Загрузка документа и создание транзакции на основе анализа
     */
    @PostMapping(value = "/create-transaction", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createTransactionFromDocument(
        @RequestParam("file") MultipartFile file,
        @RequestParam(required = false) String assetId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        try {
            // Анализируем документ
            DocumentAnalysisResult analysis = documentAnalysisService.analyzeDocument(file);

            // Здесь будет логика создания транзакции на основе анализа
            // Для примера возвращаем данные анализа
            Map<String, Object> response = new HashMap<>();
            response.put("analysis", analysis);
            response.put("message", "Документ проанализирован. Готово к созданию транзакции.");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Ошибка при создании транзакции из документа: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Информация о поддерживаемых форматах
     */
    @GetMapping("/supported-formats")
    public ResponseEntity<Map<String, Object>> getSupportedFormats() {
        Map<String, Object> response = new HashMap<>();
        response.put("formats", new String[] {
            "PDF - чеки, квитанции, счета",
            "PNG - скриншоты, фото чеков",
            "JPG/JPEG - фото чеков, квитанций"
        });
        response.put("maxFileSize", "10MB");
        response.put("features", new String[] {
            "Авто-определение валюты",
            "Распознавание суммы",
            "Извлечение даты",
            "Определение типа документа",
            "Распознавание позиций (для чеков)",
            "Интеграция с ИИ для анализа"
        });
        return ResponseEntity.ok(response);
    }
}
