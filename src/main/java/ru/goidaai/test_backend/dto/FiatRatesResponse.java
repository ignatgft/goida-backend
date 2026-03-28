package ru.goidaai.test_backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import ru.goidaai.test_backend.service.rates.ExchangeRatesResult;

/**
 * Ответ с курсами фиатных валют
 */
public record FiatRatesResponse(
    String base,
    Instant retrievedAt,
    Map<String, BigDecimal> rates,
    String source
) {
    public static FiatRatesResponse from(ExchangeRatesResult result) {
        return new FiatRatesResponse(
            result.baseCurrency(),
            result.retrievedAt(),
            result.rates(),
            result.source()
        );
    }
}
