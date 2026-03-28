package ru.goidaai.test_backend.dto.analytics;

import java.math.BigDecimal;

/**
 * DTO для статуса бюджета
 */
public record BudgetStatusDTO(
    BigDecimal budget,
    BigDecimal spent,
    BigDecimal remaining,
    BigDecimal percentageUsed,
    BigDecimal dailyAverage,
    BigDecimal projectedEndOfMonth
) {
    public static BudgetStatusDTO empty() {
        return new BudgetStatusDTO(
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO
        );
    }
}
