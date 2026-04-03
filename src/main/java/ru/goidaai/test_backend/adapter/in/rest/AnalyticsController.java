package ru.goidaai.test_backend.adapter.in.rest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.goidaai.test_backend.dto.analytics.CategorySpendingDTO;
import ru.goidaai.test_backend.dto.analytics.SpendingTrendDTO;
import ru.goidaai.test_backend.model.User;
import ru.goidaai.test_backend.model.enums.PeriodFilter;
import ru.goidaai.test_backend.service.CurrentUserService;
import ru.goidaai.test_backend.service.RatesService;
import ru.goidaai.test_backend.service.analytics.AnalyticsService;

/**
 * Расширенная аналитика трат пользователя
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final CurrentUserService currentUserService;
    private final RatesService ratesService;

    public AnalyticsController(
        AnalyticsService analyticsService,
        CurrentUserService currentUserService,
        RatesService ratesService
    ) {
        this.analyticsService = analyticsService;
        this.currentUserService = currentUserService;
        this.ratesService = ratesService;
    }

    /**
     * Получить полную аналитику трат
     */
    @GetMapping("/spending")
    public ResponseEntity<Map<String, Object>> getSpendingAnalytics(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(defaultValue = "month") String period
    ) {
        User user = currentUserService.require(jwt.getSubject());
        PeriodFilter periodFilter = PeriodFilter.from(period);

        List<CategorySpendingDTO> byCategory = analyticsService.getSpendingByCategory(user.getId(), periodFilter);
        SpendingTrendDTO trend = analyticsService.getSpendingTrend(user.getId(), periodFilter);
        var budgetStatus = analyticsService.getBudgetStatus(user.getId(), periodFilter);

        // Топ-5 самых больших трат
        var topExpenses = analyticsService.getTopExpenses(user.getId(), periodFilter, 5);

        // Средний чек
        var avgCheck = analyticsService.getAverageCheck(user.getId(), periodFilter);

        return ResponseEntity.ok(Map.of(
            "period", periodFilter.label(),
            "byCategory", byCategory,
            "trend", trend,
            "budgetStatus", budgetStatus,
            "topExpenses", topExpenses,
            "averageCheck", avgCheck
        ));
    }

    /**
     * Получить аналитику по конкретной категории
     */
    @GetMapping("/spending/category")
    public ResponseEntity<Map<String, Object>> getCategoryAnalytics(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam String category,
        @RequestParam(defaultValue = "month") String period
    ) {
        User user = currentUserService.require(jwt.getSubject());
        PeriodFilter periodFilter = PeriodFilter.from(period);

        var categoryAnalytics = analyticsService.getCategoryDetails(user.getId(), category, periodFilter);

        return ResponseEntity.ok(categoryAnalytics);
    }
}
