package ru.goidaai.test_backend.service.transaction;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.goidaai.test_backend.dto.CreateTransactionRequest;
import ru.goidaai.test_backend.model.Receipt;
import ru.goidaai.test_backend.model.enums.TransactionKind;

/**
 * Сервис для формирования заголовков транзакций
 */
@Component
public class TransactionTitleResolver {

    /**
     * Определить заголовок транзакции
     */
    public String resolveTitle(CreateTransactionRequest request, Receipt receipt) {
        // Явно указанный заголовок имеет приоритет
        if (StringUtils.hasText(request.title())) {
            return request.title().trim();
        }

        // Заголовок из чека
        if (receipt != null && StringUtils.hasText(receipt.getMerchant())) {
            return receipt.getMerchant();
        }

        // Заголовок по умолчанию на основе категории
        String category = request.category();
        if (StringUtils.hasText(category)) {
            return capitalize(category);
        }

        // Заголовок по умолчанию на основе типа
        return switch (request.kind()) {
            case EXPENSE -> "Expense";
            case INCOME -> "Income";
            case TRANSFER -> "Transfer";
        };
    }

    /**
     * Определить заголовок для существующей транзакции при обновлении
     */
    public String resolveTitleForUpdate(CreateTransactionRequest request, String currentTitle) {
        if (StringUtils.hasText(request.title())) {
            return request.title().trim();
        }
        return currentTitle;
    }

    private String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }
}
