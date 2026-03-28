package ru.goidaai.test_backend.service.rates;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * Результат получения курсов валют
 */
public record ExchangeRatesResult(
    String baseCurrency,
    Map<String, BigDecimal> rates,
    Instant retrievedAt,
    String source
) {
}
