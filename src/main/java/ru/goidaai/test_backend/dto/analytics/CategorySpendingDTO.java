package ru.goidaai.test_backend.dto.analytics;

import java.math.BigDecimal;

/**
 * DTO для статистики расходов по категории
 */
public record CategorySpendingDTO(
    String category,
    String categoryLabel,
    BigDecimal amount,
    BigDecimal percentage,
    BigDecimal percentageOfBudget,
    Long transactionCount
) {
    public static CategorySpendingDTO empty(String category) {
        return new CategorySpendingDTO(category, category, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L);
    }
}
