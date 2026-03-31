package ru.goidaai.test_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.goidaai.test_backend.dto.DocumentAnalysisResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;

/**
 * Сервис для анализа документов (чеки, квитанции, скриншоты банков)
 * Использует OCR и паттерны для извлечения данных
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentAnalysisService {

    private final GroqClient groqClient;

    @Value("${app.ai.provider:groq}")
    private String aiProvider;

    // Паттерны для определения валют
    private static final Map<String, String> CURRENCY_PATTERNS = Map.ofEntries(
        Map.entry("USD|\\$|US\\$", "USD"),
        Map.entry("EUR|€|EURO", "EUR"),
        Map.entry("RUB|₽|RUB|руб", "RUB"),
        Map.entry("KZT|₸|KZT", "KZT"),
        Map.entry("GBP|£|GBP", "GBP"),
        Map.entry("JPY|¥|JPY", "JPY"),
        Map.entry("CNY|¥|CNY", "CNY"),
        Map.entry("CHF|CHF", "CHF"),
        Map.entry("BTC|₿|BTC", "BTC"),
        Map.entry("ETH|Ξ|ETH", "ETH"),
        Map.entry("USDT|USDT", "USDT")
    );

    // Паттерны для сумм
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
        "(?:total|сумма|итого|amount|всего)[:\\s]*([\\d\\s,.]+)",
        Pattern.CASE_INSENSITIVE
    );

    // Паттерны для дат
    private static final List<Pattern> DATE_PATTERNS = List.of(
        Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4})"),
        Pattern.compile("(\\d{4}-\\d{2}-\\d{2})"),
        Pattern.compile("(\\d{2}/\\d{2}/\\d{4})")
    );

    /**
     * Анализ загруженного документа
     */
    public DocumentAnalysisResult analyzeDocument(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String fileType = file.getContentType();
        long fileSize = file.getSize();

        log.info("Анализ документа: {} ({} bytes, {})", fileName, fileSize, fileType);

        // Извлекаем текст из документа
        String extractedText = extractTextFromFile(file, fileType);

        // Анализируем текст с помощью ИИ
        return analyzeWithAI(extractedText, fileName, fileType, fileSize);
    }

    /**
     * Извлечение текста из файла (PDF или изображение)
     */
    private String extractTextFromFile(MultipartFile file, String fileType) throws IOException {
        if (fileType == null) {
            throw new IOException("Не удалось определить тип файла");
        }

        if (fileType.contains("pdf")) {
            return extractTextFromPDF(file);
        } else if (fileType.contains("image")) {
            return extractTextFromImage(file);
        } else {
            throw new IOException("Неподдерживаемый тип файла: " + fileType);
        }
    }

    /**
     * Извлечение текста из PDF
     */
    private String extractTextFromPDF(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Извлечение текста из изображения (OCR)
     * В production здесь должна быть интеграция с Tesseract или Google Vision
     */
    private String extractTextFromImage(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        
        // В production: использовать Tesseract OCR или Google Cloud Vision
        // Для демонстрации возвращаем заглушку
        log.warn("OCR не настроен. В production используйте Tesseract или Google Cloud Vision");
        
        // Здесь должна быть реальная OCR обработка
        // Для примера возвращаем пустой текст
        return "";
    }

    /**
     * Анализ текста с помощью ИИ
     */
    private DocumentAnalysisResult analyzeWithAI(String text, String fileName, 
                                                  String fileType, long fileSize) {
        // Определяем валюту
        String currency = detectCurrency(text);

        // Определяем сумму
        BigDecimal totalAmount = extractAmount(text);

        // Определяем дату
        LocalDateTime documentDate = extractDate(text);

        // Определяем тип документа
        String documentType = detectDocumentType(text);

        // Извлекаем название мерчанта
        String merchantName = extractMerchantName(text);

        // Формируем запрос к ИИ для детального анализа
        String aiAnalysis = analyzeWithGroq(text, documentType, currency);

        // Парсим ответ ИИ
        Map<String, Object> parsedAnalysis = parseAIResponse(aiAnalysis);

        // Создаем результат
        return new DocumentAnalysisResult(
            documentType,
            merchantName,
            extractMerchantAddress(text),
            documentDate,
            currency,
            totalAmount,
            extractTaxAmount(text),
            extractLineItems(parsedAnalysis),
            calculateConfidence(text),
            text,
            generateWarnings(text, currency, totalAmount),
            new DocumentAnalysisResult.Metadata(
                fileName,
                fileType,
                fileSize,
                1, // pageCount
                null, // width
                null  // height
            )
        );
    }

    /**
     * Определение валюты по тексту
     */
    private String detectCurrency(String text) {
        if (text == null || text.isEmpty()) {
            return "USD"; // Default
        }

        String upperText = text.toUpperCase();

        for (Map.Entry<String, String> entry : CURRENCY_PATTERNS.entrySet()) {
            if (Pattern.compile(entry.getKey()).matcher(upperText).find()) {
                log.info("Обнаружена валюта: {}", entry.getValue());
                return entry.getValue();
            }
        }

        // Определяем по локали чисел
        if (text.matches(".*\\d+\\.\\d{2}.*")) {
            return "USD"; // Точка как разделитель
        } else if (text.matches(".*\\d+,\\d{2}.*")) {
            return "EUR"; // Запятая как разделитель
        }

        return "USD";
    }

    /**
     * Извлечение суммы
     */
    private BigDecimal extractAmount(String text) {
        if (text == null || text.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Matcher matcher = AMOUNT_PATTERN.matcher(text);
        if (matcher.find()) {
            String amountStr = matcher.group(1).replaceAll("\\s", "");
            try {
                // Заменяем запятую на точку для парсинга
                amountStr = amountStr.replace(',', '.');
                return new BigDecimal(amountStr);
            } catch (NumberFormatException e) {
                log.warn("Не удалось распарсить сумму: {}", amountStr);
            }
        }

        // Ищем другие паттерны сумм
        Pattern totalPattern = Pattern.compile(
            "(?:TOTAL|ИТОГО|ВСЕГО|AMOUNT)[:\\s]*[\\$€₽₸£]?([\\d,.]+)",
            Pattern.CASE_INSENSITIVE
        );
        matcher = totalPattern.matcher(text);
        if (matcher.find()) {
            try {
                return new BigDecimal(matcher.group(1).replace(',', '.'));
            } catch (NumberFormatException e) {
                log.warn("Не удалось распарсить сумму: {}", matcher.group(1));
            }
        }

        return BigDecimal.ZERO;
    }

    /**
     * Извлечение даты
     */
    private LocalDateTime extractDate(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        for (Pattern pattern : DATE_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                try {
                    String dateStr = matcher.group(1);
                    if (dateStr.contains(".")) {
                        return LocalDateTime.parse(dateStr, 
                            DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    } else if (dateStr.contains("-")) {
                        return LocalDateTime.parse(dateStr, 
                            DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    } else if (dateStr.contains("/")) {
                        return LocalDateTime.parse(dateStr, 
                            DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    }
                } catch (Exception e) {
                    log.warn("Не удалось распарсить дату: {}", matcher.group(1));
                }
            }
        }

        return null;
    }

    /**
     * Определение типа документа
     */
    private String detectDocumentType(String text) {
        if (text == null || text.isEmpty()) {
            return "UNKNOWN";
        }

        String upperText = text.toUpperCase();

        if (upperText.contains("ЧЕК") || upperText.contains("RECEIPT") || 
            upperText.contains("ПРИХОДНЫЙ")) {
            return "RECEIPT";
        } else if (upperText.contains("СЧЕТ") || upperText.contains("INVOICE") ||
                   upperText.contains("КВИТАНЦИЯ")) {
            return "INVOICE";
        } else if (upperText.contains("ВЫПИСКА") || upperText.contains("STATEMENT") ||
                   upperText.contains("БАНК")) {
            return "BANK_STATEMENT";
        } else if (upperText.contains("ПЕРЕВОД") || upperText.contains("TRANSFER")) {
            return "TRANSFER";
        }

        return "OTHER";
    }

    /**
     * Извлечение названия мерчанта
     */
    private String extractMerchantName(String text) {
        if (text == null || text.isEmpty()) {
            return "Неизвестно";
        }

        // Ищем название в первых строках
        String[] lines = text.split("\n");
        if (lines.length > 0) {
            return lines[0].trim();
        }

        return "Неизвестно";
    }

    /**
     * Извлечение адреса мерчанта
     */
    private String extractMerchantAddress(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        Pattern addressPattern = Pattern.compile(
            "(?:ул\\.?|улица|пр\\.?|проспект|пер\\.?|переулок|д\\.?|дом).*",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = addressPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }

    /**
     * Извлечение суммы налога
     */
    private BigDecimal extractTaxAmount(String text) {
        if (text == null || text.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Pattern taxPattern = Pattern.compile(
            "(?:НДС|TAX|IVA)[:\\s]*([\\d,.]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = taxPattern.matcher(text);
        if (matcher.find()) {
            try {
                return new BigDecimal(matcher.group(1).replace(',', '.'));
            } catch (NumberFormatException e) {
                log.warn("Не удалось распарсить сумму налога: {}", matcher.group(1));
            }
        }

        return BigDecimal.ZERO;
    }

    /**
     * Извлечение позиций документа
     */
    private List<DocumentAnalysisResult.LineItem> extractLineItems(
        Map<String, Object> parsedAnalysis) {
        // Парсим позиции из ответа ИИ
        // В production здесь будет реальный парсинг JSON от ИИ
        return new ArrayList<>();
    }

    /**
     * Расчет уверенности распознавания
     */
    private String calculateConfidence(String text) {
        if (text == null || text.isEmpty()) {
            return "0.0";
        }

        // Простая эвристика: чем больше текста, тем выше уверенность
        int length = text.length();
        if (length > 500) return "0.9";
        if (length > 200) return "0.7";
        if (length > 50) return "0.5";
        return "0.3";
    }

    /**
     * Генерация предупреждений
     */
    private List<String> generateWarnings(String text, String currency, 
                                           BigDecimal totalAmount) {
        List<String> warnings = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            warnings.add("Текст не распознан. Возможно, низкое качество изображения.");
        }

        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            warnings.add("Не удалось определить сумму.");
        }

        if ("UNKNOWN".equals(detectDocumentType(text))) {
            warnings.add("Не удалось определить тип документа.");
        }

        return warnings;
    }

    /**
     * Анализ с помощью Groq AI
     */
    private String analyzeWithGroq(String text, String documentType, String currency) {
        if (!"groq".equals(aiProvider)) {
            return "{}";
        }

        String prompt = String.format("""
            Проанализируй текст документа и извлеки следующие данные в формате JSON:
            
            Тип документа: %s
            Валюта: %s
            
            Текст документа:
            %s
            
            Верни JSON с полями:
            - lineItems: массив позиций (name, quantity, unitPrice, totalPrice, category)
            - merchantName: название организации
            - merchantAddress: адрес
            - documentDate: дата
            - totalAmount: общая сумма
            - taxAmount: сумма налога
            
            Только JSON, без дополнительного текста.
            """, documentType, currency, text);

        try {
            return groqClient.send(prompt);
        } catch (Exception e) {
            log.error("Ошибка при анализе через Groq: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * Парсинг ответа ИИ
     */
    private Map<String, Object> parseAIResponse(String response) {
        // В production здесь будет реальный JSON парсинг
        return new HashMap<>();
    }
}
