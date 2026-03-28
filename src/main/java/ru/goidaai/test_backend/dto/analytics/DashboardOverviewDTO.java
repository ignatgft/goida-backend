package ru.goidaai.test_backend.dto.analytics;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import ru.goidaai.test_backend.dto.AssetDTO;
import ru.goidaai.test_backend.dto.SpendingDTO;

/**
 * Расширенное DTO для дашборда с аналитикой
 */
public record DashboardOverviewDTO(
    String userId,
    String baseCurrency,
    String periodLabel,
    List<AssetDTO> assets,
    SpendingDTO spending,
    BudgetStatusDTO budgetStatus,
    List<CategorySpendingDTO> categoryBreakdown,
    SpendingTrendDTO trend,
    Map<String, BigDecimal> spendingPercentages,
    BigDecimal totalNetWorth
) {
}
