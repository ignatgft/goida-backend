package ru.goidaai.test_backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.goidaai.test_backend.dto.AssetDTO;
import ru.goidaai.test_backend.dto.SpendingDTO;
import ru.goidaai.test_backend.dto.analytics.BudgetStatusDTO;
import ru.goidaai.test_backend.dto.analytics.CategorySpendingDTO;
import ru.goidaai.test_backend.dto.analytics.DashboardOverviewDTO;
import ru.goidaai.test_backend.dto.analytics.SpendingTrendDTO;
import ru.goidaai.test_backend.model.Transaction;
import ru.goidaai.test_backend.model.User;
import ru.goidaai.test_backend.model.enums.PeriodFilter;
import ru.goidaai.test_backend.model.enums.TransactionKind;
import ru.goidaai.test_backend.repository.TransactionRepository;
import ru.goidaai.test_backend.service.analytics.AnalyticsService;

/**
 * Сервис для получения общей сводки дашборда
 */
@Service
public class DashboardService {

    private final CurrentUserService currentUserService;
    private final AssetsService assetsService;
    private final TransactionRepository transactionRepository;
    private final AnalyticsService analyticsService;
    private final RatesService ratesService;
    private final Clock clock;

    public DashboardService(
        CurrentUserService currentUserService,
        AssetsService assetsService,
        TransactionRepository transactionRepository,
        AnalyticsService analyticsService,
        RatesService ratesService,
        Clock clock
    ) {
        this.currentUserService = currentUserService;
        this.assetsService = assetsService;
        this.transactionRepository = transactionRepository;
        this.analyticsService = analyticsService;
        this.ratesService = ratesService;
        this.clock = clock;
    }

    /**
     * Получить расширенную сводку дашборда
     */
    @Transactional(readOnly = true)
    public DashboardOverviewDTO getOverview(String userId, String rawPeriod) {
        User user = currentUserService.require(userId);
        PeriodFilter period = PeriodFilter.from(rawPeriod);

        // Базовые данные
        List<AssetDTO> assets = assetsService.listForUser(user);
        
        // Расходы за период
        List<Transaction> expenses = transactionRepository.findAll(expensesSpec(userId, period));
        BigDecimal spent = expenses.stream()
            .map(t -> ratesService.convertAmount(t.getAmount(), t.getCurrency(), user.getBaseCurrency()))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal budget = period.budgetFromMonthly(user.getMonthlyBudget());
        SpendingDTO spending = new SpendingDTO(spent, budget);

        // Аналитика
        BudgetStatusDTO budgetStatus = analyticsService.getBudgetStatus(userId, period);
        List<CategorySpendingDTO> categoryBreakdown = analyticsService.getSpendingByCategory(userId, period);
        SpendingTrendDTO trend = analyticsService.getSpendingTrend(userId, period);

        // Проценты расходов
        var spendingPercentages = categoryBreakdown.stream()
            .collect(java.util.stream.Collectors.toMap(
                CategorySpendingDTO::category,
                CategorySpendingDTO::percentage
            ));

        // Общий net worth (сумма всех активов в базовой валюте)
        BigDecimal totalNetWorth = assets.stream()
            .map(AssetDTO::currentValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardOverviewDTO(
            userId,
            user.getBaseCurrency(),
            period.label(),
            assets,
            spending,
            budgetStatus,
            categoryBreakdown,
            trend,
            spendingPercentages,
            totalNetWorth
        );
    }

    /**
     * Получить упрощённую сводку (для обратной совместимости)
     */
    @Transactional(readOnly = true)
    public ru.goidaai.test_backend.dto.analytics.DashboardOverviewDTO getSimpleOverview(String userId, String rawPeriod) {
        User user = currentUserService.require(userId);
        PeriodFilter period = PeriodFilter.from(rawPeriod);
        List<AssetDTO> assets = assetsService.listForUser(user);

        List<Transaction> expenses = transactionRepository.findAll(expensesSpec(userId, period));
        BigDecimal spent = expenses.stream()
            .map(t -> ratesService.convertAmount(t.getAmount(), t.getCurrency(), user.getBaseCurrency()))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal budget = period.budgetFromMonthly(user.getMonthlyBudget());
        return new ru.goidaai.test_backend.dto.analytics.DashboardOverviewDTO(
            userId,
            user.getBaseCurrency(),
            period.label(),
            assets,
            new SpendingDTO(spent, budget),
            BudgetStatusDTO.empty(),
            List.of(),
            null,
            Map.of(),
            BigDecimal.ZERO
        );
    }

    /**
     * Спецификация для фильтрации расходов
     */
    private Specification<Transaction> expensesSpec(String userId, PeriodFilter period) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
            criteriaBuilder.equal(root.get("user").get("id"), userId),
            criteriaBuilder.equal(root.get("kind"), TransactionKind.EXPENSE),
            criteriaBuilder.greaterThanOrEqualTo(root.get("occurredAt"), period.startsAt(clock))
        );
    }
}
