package ru.goidaai.test_backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.goidaai.test_backend.config.AppProperties;
import ru.goidaai.test_backend.dto.ChatMessageDTO;
import ru.goidaai.test_backend.model.ChatHistory;
import ru.goidaai.test_backend.model.Transaction;
import ru.goidaai.test_backend.model.User;
import ru.goidaai.test_backend.model.enums.PeriodFilter;
import ru.goidaai.test_backend.model.enums.TransactionKind;
import ru.goidaai.test_backend.repository.AssetRepository;
import ru.goidaai.test_backend.repository.ChatHistoryRepository;
import ru.goidaai.test_backend.repository.TransactionRepository;

@Service
public class ChatService {

    private final CurrentUserService currentUserService;
    private final AssetRepository assetRepository;
    private final TransactionRepository transactionRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final RatesService ratesService;
    private final DtoFactory dtoFactory;
    private final AppProperties appProperties;
    private final Clock clock;
    private final GroqClient groqClient;

    public ChatService(
        CurrentUserService currentUserService,
        AssetRepository assetRepository,
        TransactionRepository transactionRepository,
        ChatHistoryRepository chatHistoryRepository,
        RatesService ratesService,
        DtoFactory dtoFactory,
        AppProperties appProperties,
        Clock clock,
        GroqClient groqClient
    ) {
        this.currentUserService = currentUserService;
        this.assetRepository = assetRepository;
        this.transactionRepository = transactionRepository;
        this.chatHistoryRepository = chatHistoryRepository;
        this.ratesService = ratesService;
        this.dtoFactory = dtoFactory;
        this.appProperties = appProperties;
        this.clock = clock;
        this.groqClient = groqClient;
    }

    @Transactional
    public ChatMessageDTO chat(String userId, String message, String userLocale) {
        User user = currentUserService.require(userId);
        List<ru.goidaai.test_backend.model.Asset> assets = assetRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        BigDecimal monthlySpent = transactionRepository.findAll(monthlyExpenseSpecification(userId)).stream()
            .map(transaction -> ratesService.convertAmount(
                transaction.getAmount(),
                transaction.getCurrency(),
                user.getBaseCurrency()
            ))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        // Получаем последние транзакции для контекста
        List<Transaction> recentTransactions = transactionRepository.findByUser_IdOrderByOccurredAtDesc(
            userId,
            org.springframework.data.domain.PageRequest.of(0, 10)
        );
        
        // Определяем язык пользователя
        String language = detectLanguage(userLocale, user.getEmail());
        
        String systemPrompt = buildSystemPrompt(user, assets.size(), monthlySpent, recentTransactions, language);
        String response = groqClient.chat(message, systemPrompt, language);
        
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setUser(user);
        chatHistory.setMessage(message.trim());
        chatHistory.setResponse(response);
        chatHistory.setProvider("groq");
        chatHistory.setModel(appProperties.getAi().getGroqModel());

        return dtoFactory.toChatMessageDto(chatHistoryRepository.save(chatHistory));
    }

    private String detectLanguage(String userLocale, String email) {
        // Приоритет: явная локаль > домен email > русский по умолчанию
        if (userLocale != null && !userLocale.isBlank()) {
            String lang = userLocale.toLowerCase().substring(0, 2);
            if ("ru".equals(lang) || "uk".equals(lang) || "be".equals(lang) || "kk".equals(lang)) {
                return "russian";
            }
            if ("en".equals(lang)) {
                return "english";
            }
        }
        // Проверяем домен email
        if (email != null && !email.isBlank()) {
            if (email.toLowerCase().matches(".*\\.(ru|ua|by|kz)$")) {
                return "russian";
            }
        }
        // По умолчанию русский для СНГ
        return "russian";
    }

    private Specification<Transaction> monthlyExpenseSpecification(String userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
            criteriaBuilder.equal(root.get("user").get("id"), userId),
            criteriaBuilder.equal(root.get("kind"), TransactionKind.EXPENSE),
            criteriaBuilder.greaterThanOrEqualTo(root.get("occurredAt"), PeriodFilter.MONTH.startsAt(clock))
        );
    }

    private String buildSystemPrompt(User user, int assetCount, BigDecimal monthlySpent, List<Transaction> recentTransactions, String language) {
        StringBuilder prompt = new StringBuilder();
        
        if ("russian".equals(language)) {
            prompt.append("Ты полезный AI финансовый ассистент для приложения Goida AI. ");
            prompt.append("Профиль пользователя: ");
            prompt.append(user.getFullName()).append(" (").append(user.getEmail()).append("). ");
            prompt.append("Базовая валюта: ").append(user.getBaseCurrency()).append(". ");
            prompt.append("У пользователя ").append(assetCount).append(" активов и потрачено ")
                .append(monthlySpent).append(" ").append(user.getBaseCurrency())
                .append(" за последние 30 дней. ");
            
            if (!recentTransactions.isEmpty()) {
                prompt.append("Последние транзакции (до 5): ");
                for (int i = 0; i < Math.min(5, recentTransactions.size()); i++) {
                    Transaction tx = recentTransactions.get(i);
                    prompt.append(String.format("%s: %s %s (%s) - %s; ",
                        tx.getOccurredAt().toString().substring(0, 10),
                        tx.getAmount(),
                        tx.getCurrency(),
                        tx.getCategory(),
                        tx.getTitle() != null ? tx.getTitle() : tx.getNote()));
                }
            }
            
            prompt.append("Давай краткие, практичные финансовые советы на русском языке. ");
            prompt.append("Ответы не более 200 слов. ");
            prompt.append("Если у пользователя нет данных, предложи добавить активы и начать отслеживать траты. ");
            prompt.append("Анализируй паттерны трат и предлагай конкретные улучшения.");
        } else {
            prompt.append("You are a helpful AI financial assistant for the Goida AI app. ");
            prompt.append("User profile: ");
            prompt.append(user.getFullName()).append(" (").append(user.getEmail()).append("). ");
            prompt.append("Base currency: ").append(user.getBaseCurrency()).append(". ");
            prompt.append("User has ").append(assetCount).append(" tracked assets and spent ")
                .append(monthlySpent).append(" ").append(user.getBaseCurrency())
                .append(" in the last 30 days. ");
            
            if (!recentTransactions.isEmpty()) {
                prompt.append("Recent transactions (last 5): ");
                for (int i = 0; i < Math.min(5, recentTransactions.size()); i++) {
                    Transaction tx = recentTransactions.get(i);
                    prompt.append(String.format("%s: %s %s (%s) - %s; ",
                        tx.getOccurredAt().toString().substring(0, 10),
                        tx.getAmount(),
                        tx.getCurrency(),
                        tx.getCategory(),
                        tx.getTitle() != null ? tx.getTitle() : tx.getNote()));
                }
            }
            
            prompt.append("Provide concise, actionable financial advice in English. ");
            prompt.append("Keep responses under 200 words. ");
            prompt.append("If the user has no data yet, encourage them to add assets and track spending. ");
            prompt.append("Analyze spending patterns and suggest specific improvements based on transaction history.");
        }
        
        return prompt.toString();
    }
}
