package ru.goidaai.test_backend.service.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.goidaai.test_backend.dto.CreateTransactionRequest;
import ru.goidaai.test_backend.exception.BadRequestException;

/**
 * Сервис для валидации данных транзакции
 */
@Component
public class TransactionValidator {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000000");
    private static final int MAX_TITLE_LENGTH = 160;
    private static final int MAX_NOTE_LENGTH = 500;
    private static final int MAX_CURRENCY_LENGTH = 10;

    /**
     * Валидировать запрос на создание транзакции
     */
    public void validateCreateRequest(CreateTransactionRequest request) {
        validateAmount(request.amount(), request.receipt());
        validateCurrency(request.currency());
        validateTitle(request.title());
        validateNote(request.note());
        validateOccurredAt(request.occurredAt());
    }

    /**
     * Валидировать сумму транзакции
     */
    public void validateAmount(BigDecimal amount, Object receipt) {
        if (amount == null && receipt == null) {
            throw new BadRequestException("Amount or receipt must be provided");
        }

        if (amount != null) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Amount must be positive");
            }
            if (amount.compareTo(MAX_AMOUNT) > 0) {
                throw new BadRequestException("Amount exceeds maximum allowed value");
            }
        }
    }

    /**
     * Валидировать валюту
     */
    public void validateCurrency(String currency) {
        if (!StringUtils.hasText(currency)) {
            throw new BadRequestException("Currency is required");
        }
        if (currency.length() > MAX_CURRENCY_LENGTH) {
            throw new BadRequestException("Currency code is too long");
        }
    }

    /**
     * Валидировать заголовок
     */
    public void validateTitle(String title) {
        if (StringUtils.hasText(title) && title.length() > MAX_TITLE_LENGTH) {
            throw new BadRequestException("Title is too long (max " + MAX_TITLE_LENGTH + " characters)");
        }
    }

    /**
     * Валидировать заметку
     */
    public void validateNote(String note) {
        if (StringUtils.hasText(note) && note.length() > MAX_NOTE_LENGTH) {
            throw new BadRequestException("Note is too long (max " + MAX_NOTE_LENGTH + " characters)");
        }
    }

    /**
     * Валидировать дату операции
     */
    public void validateOccurredAt(Instant occurredAt) {
        if (occurredAt != null && occurredAt.isAfter(Instant.now().plusSeconds(3600))) {
            throw new BadRequestException("Transaction date cannot be in the future");
        }
    }

    /**
     * Обрезать и проверить null строку
     */
    public String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
