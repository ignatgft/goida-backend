package ru.goidaai.test_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.goidaai.test_backend.config.AppProperties;
import ru.goidaai.test_backend.dto.DocumentAnalysisResult;

@Component
public class GroqClient {

    private static final Logger log = LoggerFactory.getLogger(GroqClient.class);
    private static final String DEFAULT_BASE_URL = "https://api.groq.com/openai/v1";
    private static final String DEFAULT_MODEL = "llama-3.3-70b-versatile";
    private static final String VISION_MODEL = "llama-3.2-90b-vision-preview";
    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    public GroqClient(AppProperties appProperties) {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();
        this.objectMapper = new ObjectMapper();
        this.apiKey = appProperties.getAi().getGroqApiKey();
        this.baseUrl = appProperties.getAi().getGroqBaseUrl();
        this.model = appProperties.getAi().getGroqModel();
    }

    public String chat(String userMessage, String systemPrompt, String language) {
        if (apiKey == null || apiKey.isBlank() || "replace-me".equals(apiKey)) {
            log.warn("Groq API key not configured, returning mock response in {}", language);
            return generateMockResponse(userMessage, language);
        }

        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                "model", model != null && !model.isBlank() ? model : DEFAULT_MODEL,
                "messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userMessage)
                ),
                "temperature", 0.7,
                "max_tokens", 1024
            ));

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create((baseUrl != null && !baseUrl.isBlank() ? baseUrl : DEFAULT_BASE_URL) + "/chat/completions"))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Groq API error: {} - {}", response.statusCode(), response.body());
                return generateMockResponse(userMessage, language);
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode choices = rootNode.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                return choices.get(0).get("message").get("content").asText();
            }

            log.warn("No choices in Groq API response");
            return generateMockResponse(userMessage, language);

        } catch (IOException | InterruptedException e) {
            log.error("Error calling Groq API", e);
            Thread.currentThread().interrupt();
            return generateMockResponse(userMessage, language);
        }
    }

    public String chatWithContext(List<Map<String, Object>> messages, String modelToUse) {
        if (apiKey == null || apiKey.isBlank() || "replace-me".equals(apiKey)) {
            log.warn("Groq API key not configured, returning mock response");
            return "Mock response with context";
        }

        try {
            String actualModel = modelToUse != null && !modelToUse.isBlank() ? modelToUse : model;
            if (actualModel == null || actualModel.isBlank()) {
                actualModel = DEFAULT_MODEL;
            }

            Map<String, Object> requestBodyMap = Map.of(
                "model", actualModel,
                "messages", messages,
                "temperature", 0.7,
                "max_tokens", 2048
            );

            String requestBody = objectMapper.writeValueAsString(requestBodyMap);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create((baseUrl != null && !baseUrl.isBlank() ? baseUrl : DEFAULT_BASE_URL) + "/chat/completions"))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Groq API error: {} - {}", response.statusCode(), response.body());
                return "Error processing request";
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode choices = rootNode.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                return choices.get(0).get("message").get("content").asText();
            }

            log.warn("No choices in Groq API response");
            return "Error processing request";

        } catch (IOException | InterruptedException e) {
            log.error("Error calling Groq API", e);
            Thread.currentThread().interrupt();
            return "Error processing request";
        }
    }

    public String chatWithImage(String userMessage, String systemPrompt, String language, List<String> imageUrls) {
        if (apiKey == null || apiKey.isBlank() || "replace-me".equals(apiKey)) {
            log.warn("Groq API key not configured, returning mock response in {}", language);
            return generateMockResponse(userMessage, language);
        }

        try {
            List<Map<String, Object>> content = new ArrayList<>();
            content.add(Map.of("type", "text", "text", userMessage));
            
            for (String imageUrl : imageUrls) {
                content.add(Map.of(
                    "type", "image_url",
                    "image_url", Map.of("url", imageUrl)
                ));
            }

            String requestBody = objectMapper.writeValueAsString(Map.of(
                "model", VISION_MODEL,
                "messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", content)
                ),
                "temperature", 0.7,
                "max_tokens", 2048
            ));

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create((baseUrl != null && !baseUrl.isBlank() ? baseUrl : DEFAULT_BASE_URL) + "/chat/completions"))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Groq API error: {} - {}", response.statusCode(), response.body());
                return generateMockResponse(userMessage, language);
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode choices = rootNode.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                return choices.get(0).get("message").get("content").asText();
            }

            log.warn("No choices in Groq API response");
            return generateMockResponse(userMessage, language);

        } catch (IOException | InterruptedException e) {
            log.error("Error calling Groq API", e);
            Thread.currentThread().interrupt();
            return generateMockResponse(userMessage, language);
        }
    }

    public DocumentAnalysisResult analyzeDocument(String userMessage, String language, List<String> imageUrls, List<String> fileUrls) {
        boolean isRussian = "russian".equals(language) || "ru".equals(language);
        
        String systemPrompt = isRussian 
            ? "Ты AI ассистент для анализа финансовых документов. Анализируй изображения чеков, квитанций, счетов и других финансовых документов. Извлекай: тип документа, сумму, дату, категорию расходов/доходов, описание. Определи, является ли это расходом или доходом."
            : "You are an AI assistant for analyzing financial documents. Analyze images of receipts, invoices, bills, and other financial documents. Extract: document type, amount, date, expense/income category, description. Determine if this is an expense or income.";

        String analysisPrompt = isRussian
            ? userMessage + "\n\nПроанализируй документ и предоставь структурированную информацию: тип документа, сумма, дата, категория, описание. Укажи, это расход или доход."
            : userMessage + "\n\nAnalyze the document and provide structured information: document type, amount, date, category, description. Indicate if this is an expense or income.";

        String response = chatWithImage(analysisPrompt, systemPrompt, language, imageUrls);
        
        DocumentAnalysisResult result = new DocumentAnalysisResult();
        result.setDocumentType(isRussian ? "Чек/Квитанция" : "Receipt/Invoice");
        result.setExtractedText(response);
        result.setIsExpenseOrIncome(true);
        result.setSuggestedCategory(isRussian ? "Прочее" : "Other");
        result.setSuggestedAmount(0.0);
        result.setDescription(response);
        result.setShouldAddToSystem(false);
        
        return result;
    }

    private String generateMockResponse(String message, String language) {
        String normalizedMessage = message.toLowerCase();
        boolean isRussian = "russian".equals(language) || "ru".equals(language);

        if (isRussian) {
            if (normalizedMessage.contains("бюджет") || normalizedMessage.contains("расход") || normalizedMessage.contains("трат")) {
                return "Судя по вашей истории транзакций, рекомендую установить месячный бюджет $500-800 на основные расходы. Отслеживайте траты еженедельно и корректируйте категории по мере необходимости. Используйте правило 50/30/20: 50% нужды, 30% желания, 20% сбережения.";
            }
            if (normalizedMessage.contains("накоп") || normalizedMessage.contains("сбереж")) {
                return "Используйте правило 50/30/20: 50% на нужды, 30% на желания и 20% на сбережения. Начните с создания резервного фонда на 3-6 месяцев расходов. Автоматизируйте накопления, чтобы это стало привычкой.";
            }
            if (normalizedMessage.contains("инвест")) {
                return "Перед инвестированием убедитесь, что у вас есть резервный фонд. Рассмотрите недорогие индексные фонды для долгосрочного роста. Диверсифицируйте по классам активов в соответствии с вашей толерантностью к риску.";
            }
            if (normalizedMessage.contains("долг") || normalizedMessage.contains("кредит")) {
                return "Приоритизируйте долги с высоким процентом (метод лавины) или начните с наименьших долгов для быстрых побед (метод снежного кома). Рассмотрите консолидацию долга, если ставки благоприятны.";
            }
            if (normalizedMessage.contains("привет") || normalizedMessage.contains("здравств")) {
                return "Здравствуйте! Я ваш AI финансовый ассистент. Я могу помочь проанализировать расходы, создать бюджет и достичь финансовых целей. Что вы хотите узнать о своих финансах?";
            }
            if (normalizedMessage.contains("трата") || normalizedMessage.contains("покупк") || normalizedMessage.contains("расход")) {
                return "Анализируя ваши последние транзакции, я замечаю паттерны в расходах. Рассмотрите возможность категоризации расходов и установки лимитов на дискреционные траты. Мелкие ежедневные расходы могут быстро накапливаться!";
            }
            return "Я могу помочь проанализировать ваши финансовые данные. Спросите о паттернах расходов, рекомендациях по бюджету, стратегиях сбережений или инвестиционных идеях. Что вы хотите изучить?";
        }

        if (normalizedMessage.contains("budget") || normalizedMessage.contains("spend")) {
            return "Based on your transaction history, I recommend setting a monthly budget of $500-800 for essential expenses. Track your spending weekly and adjust categories as needed. Consider using the 50/30/20 rule: 50% needs, 30% wants, 20% savings.";
        }
        if (normalizedMessage.contains("save")) {
            return "Consider the 50/30/20 rule: 50% for needs, 30% for wants, and 20% for savings. Start with an emergency fund covering 3-6 months of expenses. Automate your savings to make it a habit.";
        }
        if (normalizedMessage.contains("invest")) {
            return "Before investing, ensure you have an emergency fund. Then consider low-cost index funds for long-term growth. Diversify across asset classes based on your risk tolerance.";
        }
        if (normalizedMessage.contains("debt")) {
            return "Prioritize high-interest debt first (avalanche method) or start with smallest debts for quick wins (snowball method). Consider debt consolidation if rates are favorable.";
        }
        if (normalizedMessage.contains("hello") || normalizedMessage.contains("hi")) {
            return "Hello! I'm your AI financial assistant. I can help you analyze spending, create budgets, and achieve your financial goals. What would you like to know about your finances?";
        }
        return "I can help you analyze your financial data. Ask me about your spending patterns, budget recommendations, savings strategies, or investment ideas. What would you like to explore?";
    }
}
