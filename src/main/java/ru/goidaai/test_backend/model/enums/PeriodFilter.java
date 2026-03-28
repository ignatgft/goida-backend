package ru.goidaai.test_backend.model.enums;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;
import ru.goidaai.test_backend.exception.BadRequestException;

public enum PeriodFilter {
    DAY,
    WEEK,
    MONTH,
    YEAR,
    ALL;

    public static PeriodFilter from(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return MONTH;
        }
        try {
            return PeriodFilter.valueOf(rawValue.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Unsupported period. Allowed values: day, week, month, year, all");
        }
    }

    public Instant startsAt(Clock clock) {
        ZonedDateTime now = ZonedDateTime.now(clock).withZoneSameInstant(ZoneOffset.UTC);
        return switch (this) {
            case DAY -> now.minusDays(1).toInstant();
            case WEEK -> now.minusWeeks(1).toInstant();
            case MONTH -> now.minusMonths(1).toInstant();
            case YEAR -> now.minusYears(1).toInstant();
            case ALL -> Instant.EPOCH;
        };
    }

    public Instant endsAt(Clock clock) {
        return ZonedDateTime.now(clock).withZoneSameInstant(ZoneOffset.UTC).toInstant();
    }

    public String label() {
        return switch (this) {
            case DAY -> "Last 24 hours";
            case WEEK -> "Last 7 days";
            case MONTH -> "Last 30 days";
            case YEAR -> "Last 12 months";
            case ALL -> "All time";
        };
    }

    public BigDecimal budgetMultiplier() {
        return switch (this) {
            case DAY -> new BigDecimal("0.0333");
            case WEEK -> new BigDecimal("0.2333");
            case MONTH -> BigDecimal.ONE;
            case YEAR -> new BigDecimal("12");
            case ALL -> BigDecimal.ONE;
        };
    }

    public BigDecimal budgetFromMonthly(BigDecimal monthlyBudget) {
        if (monthlyBudget == null) {
            return BigDecimal.ZERO;
        }
        if (this == ALL) {
            return monthlyBudget;
        }
        return monthlyBudget.multiply(budgetMultiplier()).setScale(2, RoundingMode.HALF_UP);
    }
}
