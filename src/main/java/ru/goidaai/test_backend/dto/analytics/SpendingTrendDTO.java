package ru.goidaai.test_backend.dto.analytics;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO для тренда расходов
 */
public record SpendingTrendDTO(
    String period,
    BigDecimal totalSpent,
    BigDecimal averageDaily,
    BigDecimal peakDayAmount,
    List<DailySpendingDTO> dailyBreakdown
) {
    public record DailySpendingDTO(
        String date,
        BigDecimal amount,
        Long transactionCount
    ) {}
}
