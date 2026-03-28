package ru.goidaai.test_backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import ru.goidaai.test_backend.service.rates.ExchangeRatesResult;

/**
 * Ответ с курсами криптовалют
 */
public record CryptoRatesResponse(
    String quoteCurrency,
    Instant retrievedAt,
    Map<String, BigDecimal> prices,
    String source
) {
    public static CryptoRatesResponse from(ExchangeRatesResult result) {
        return new CryptoRatesResponse(
            result.baseCurrency(),
            result.retrievedAt(),
            result.rates(),
            result.source()
        );
    }
}
