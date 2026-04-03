package ru.goidaai.test_backend.service.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.goidaai.test_backend.dto.analytics.BudgetStatusDTO;
import ru.goidaai.test_backend.dto.analytics.CategorySpendingDTO;
import ru.goidaai.test_backend.dto.analytics.SpendingTrendDTO;
import ru.goidaai.test_backend.dto.analytics.SpendingTrendDTO.DailySpendingDTO;
import ru.goidaai.test_backend.model.Transaction;
import ru.goidaai.test_backend.model.User;
import ru.goidaai.test_backend.model.enums.PeriodFilter;
import ru.goidaai.test_backend.model.enums.TransactionKind;
import ru.goidaai.test_backend.repository.TransactionRepository;
import ru.goidaai.test_backend.service.CurrentUserService;
import ru.goidaai.test_backend.service.RatesService;

/**
 * Сервис для аналитики расходов и статистики
 */
@Service
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;
    private final RatesService ratesService;
    private final Clock clock;
    private final DateTimeFormatter dateFormatter;

    public AnalyticsService(
        TransactionRepository transactionRepository,
        CurrentUserService currentUserService,
        RatesService ratesService,
        Clock clock
    ) {
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
        this.ratesService = ratesService;
        this.clock = clock;
        this.dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    /**
     * Получить статистику расходов по категориям
     */
    @Transactional(readOnly = true)
    public List<CategorySpendingDTO> getSpendingByCategory(String userId, PeriodFilter period) {
        User user = currentUserService.require(userId);
        List<Transaction> expenses = transactionRepository.findAll(expensesSpec(userId, period));
        return getSpendingByCategory(user, expenses);
    }

    /**
     * ОПТИМИЗИРОВАНО: Статистика с предзагруженными транзакциями
     */
    public List<CategorySpendingDTO> getSpendingByCategory(User user, List<Transaction> expenses) {

        // Группировка по категориям с конвертацией в базовую валюту
        Map<String, List<Transaction>> byCategory = expenses.stream()
            .collect(Collectors.groupingBy(Transaction::getCategory));

        BigDecimal totalSpent = expenses.stream()
            .map(t -> ratesService.convertAmount(t.getAmount(), t.getCurrency(), user.getBaseCurrency()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategorySpendingDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<Transaction>> entry : byCategory.entrySet()) {
            String category = entry.getKey();
            List<Transaction> transactions = entry.getValue();

            BigDecimal amount = transactions.stream()
                .map(t -> ratesService.convertAmount(t.getAmount(), t.getCurrency(), user.getBaseCurrency()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal percentage = totalSpent.compareTo(BigDecimal.ZERO) > 0
                ? amount.multiply(BigDecimal.valueOf(100)).divide(totalSpent, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

            BigDecimal percentageOfBudget = period.budgetFromMonthly(user.getMonthlyBudget())
                .compareTo(BigDecimal.ZERO) > 0
                ? amount.multiply(BigDecimal.valueOf(100))
                    .divide(period.budgetFromMonthly(user.getMonthlyBudget()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

            result.add(new CategorySpendingDTO(
                category,
                localizeCategory(category),
                amount.setScale(2, RoundingMode.HALF_UP),
                percentage.setScale(2, RoundingMode.HALF_UP),
                percentageOfBudget.setScale(2, RoundingMode.HALF_UP),
                (long) transactions.size()
            ));
        }

        // Сортировка по убыванию суммы
        result.sort(Comparator.comparing(CategorySpendingDTO::amount).reversed());
        return result;
    }

    /**
     * Получить тренд расходов по дням
     */
    @Transactional(readOnly = true)
    public SpendingTrendDTO getSpendingTrend(String userId, PeriodFilter period) {
        User user = currentUserService.require(userId);
        List<Transaction> expenses = transactionRepository.findAll(expensesSpec(userId, period));
        return getSpendingTrend(user, expenses, period);
    }

    /**
     * ОПТИМИЗИРОВАНО: Тренд расходов с предзагруженными транзакциями
     */
    public SpendingTrendDTO getSpendingTrend(User user, List<Transaction> expenses, PeriodFilter period) {

        // Группировка по дням
        Map<LocalDate, List<Transaction>> byDate = expenses.stream()
            .collect(Collectors.groupingBy(t -> 
                t.getOccurredAt().atZone(ZoneId.systemDefault()).toLocalDate()
            ));

        LocalDate startDate = period.startsAt(clock).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = LocalDate.now(clock);

        List<DailySpendingDTO> dailyBreakdown = new ArrayList<>();
        BigDecimal totalSpent = BigDecimal.ZERO;
        BigDecimal peakDayAmount = BigDecimal.ZERO;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<Transaction> dayTransactions = byDate.getOrDefault(date, List.of());
            BigDecimal dayAmount = dayTransactions.stream()
                .map(t -> ratesService.convertAmount(t.getAmount(), t.getCurrency(), user.getBaseCurrency()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            dailyBreakdown.add(new DailySpendingDTO(
                date.format(dateFormatter),
                dayAmount.setScale(2, RoundingMode.HALF_UP),
                (long) dayTransactions.size()
            ));

            totalSpent = totalSpent.add(dayAmount);
            if (dayAmount.compareTo(peakDayAmount) > 0) {
                peakDayAmount = dayAmount;
            }
        }

        long daysCount = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal averageDaily = daysCount > 0
            ? totalSpent.divide(BigDecimal.valueOf(daysCount), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        return new SpendingTrendDTO(
            period.label(),
            totalSpent.setScale(2, RoundingMode.HALF_UP),
            averageDaily,
            peakDayAmount.setScale(2, RoundingMode.HALF_UP),
            dailyBreakdown
        );
    }

    /**
     * Получить статус бюджета
     */
    @Transactional(readOnly = true)
    public BudgetStatusDTO getBudgetStatus(String userId, PeriodFilter period) {
        User user = currentUserService.require(userId);
        List<Transaction> expenses = transactionRepository.findAll(expensesSpec(userId, period));
        return getBudgetStatus(user, expenses);
    }

    /**
     * ОПТИМИЗИРОВАНО: Статус бюджета с предзагруженными транзакциями
     */
    public BudgetStatusDTO getBudgetStatus(User user, List<Transaction> expenses) {

        BigDecimal spent = expenses.stream()
            .map(t -> ratesService.convertAmount(t.getAmount(), t.getCurrency(), user.getBaseCurrency()))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal budget = period.budgetFromMonthly(user.getMonthlyBudget()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal remaining = budget.subtract(spent).max(BigDecimal.ZERO);

        BigDecimal percentageUsed = budget.compareTo(BigDecimal.ZERO) > 0
            ? spent.multiply(BigDecimal.valueOf(100)).divide(budget, 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        // Расчёт средней дневной траты
        LocalDate startDate = period.startsAt(clock).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = LocalDate.now(clock);
        long daysPassed = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        
        BigDecimal dailyAverage = daysPassed > 0
            ? spent.divide(BigDecimal.valueOf(daysPassed), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        // Прогноз расходов до конца месяца
        LocalDate periodEnd = period.endsAt(clock).atZone(ZoneId.systemDefault()).toLocalDate();
        long totalDaysInPeriod = ChronoUnit.DAYS.between(startDate, periodEnd) + 1;
        BigDecimal projectedEndOfMonth = dailyAverage.multiply(BigDecimal.valueOf(totalDaysInPeriod))
            .setScale(2, RoundingMode.HALF_UP);

        return new BudgetStatusDTO(
            budget,
            spent,
            remaining,
            percentageUsed,
            dailyAverage,
            projectedEndOfMonth
        );
    }

    /**
     * Получить проценты расходов по категориям
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getSpendingPercentages(String userId, PeriodFilter period) {
        List<CategorySpendingDTO> breakdown = getSpendingByCategory(userId, period);
        return breakdown.stream()
            .collect(Collectors.toMap(
                CategorySpendingDTO::category,
                CategorySpendingDTO::percentage
            ));
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

    /**
     * Локализация названия категории (упрощённая версия)
     */
    private String localizeCategory(String category) {
        return switch (category.toLowerCase()) {
            case "food" -> "Еда";
            case "transport" -> "Транспорт";
            case "shopping" -> "Покупки";
            case "bills" -> "Счета";
            case "crypto" -> "Криптовалюта";
            case "subscriptions" -> "Подписки";
            case "entertainment" -> "Развлечения";
            case "health" -> "Здоровье";
            case "education" -> "Обучение";
            case "travel" -> "Путешествия";
            case "other" -> "Другое";
            default -> category;
        };
    }
}
