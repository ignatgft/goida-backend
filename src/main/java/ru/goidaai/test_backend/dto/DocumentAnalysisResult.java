package ru.goidaai.test_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для результата анализа документа (чек, квитанция, скриншот)
 */
public record DocumentAnalysisResult(
    String documentType,           // Тип документа: RECEIPT, BANK_STATEMENT, INVOICE, etc.
    String merchantName,           // Название магазина/организации
    String merchantAddress,        // Адрес
    LocalDateTime documentDate,    // Дата документа
    String currency,               // Валюта (авто-определение)
    BigDecimal totalAmount,        // Общая сумма
    BigDecimal taxAmount,          // Сумма налога
    List<LineItem> lineItems,      // Позиции документа
    String confidence,             // Уверенность распознавания (0.0 - 1.0)
    String rawText,                // Распознанный текст
    List<String> warnings,         // Предупреждения о проблемах распознавания
    Metadata metadata              // Метаданные документа
) {
    public record LineItem(
        String name,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        String category
    ) {}
    
    public record Metadata(
        String fileName,
        String fileType,
        Long fileSize,
        Integer pageCount,
        Integer width,
        Integer height
    ) {}
}
