package ru.goidaai.test_backend.service.rates;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Кэш для курсов валют
 * Хранит курсы в памяти с TTL
 */
@Component
public class RatesCache {

    private static final Logger log = LoggerFactory.getLogger(RatesCache.class);
    
    // Время жизни кэша: 1 час для фиата, 5 минут для крипты
    private static final Duration FIAT_CACHE_TTL = Duration.ofHours(1);
    private static final Duration CRYPTO_CACHE_TTL = Duration.ofMinutes(5);

    private final Map<String, CachedRates> cache = new ConcurrentHashMap<>();

    /**
     * Получить курсы из кэша
     */
    public ExchangeRatesResult get(String key) {
        CachedRates cached = cache.get(key);
        if (cached == null) {
            return null;
        }

        Duration ttl = cached.isCrypto() ? CRYPTO_CACHE_TTL : FIAT_CACHE_TTL;
        if (Instant.now().isAfter(cached.expiresAt())) {
            log.debug("Cache expired for {}", key);
            cache.remove(key);
            return null;
        }

        log.debug("Cache hit for {}", key);
        return cached.result();
    }

    /**
     * Сохранить курсы в кэш
     */
    public void put(String key, ExchangeRatesResult result, boolean isCrypto) {
        Instant expiresAt = Instant.now().plus(isCrypto ? CRYPTO_CACHE_TTL : FIAT_CACHE_TTL);
        cache.put(key, new CachedRates(result, isCrypto, expiresAt));
        log.debug("Cached {} rates for {} until {}", result.source(), result.baseCurrency(), expiresAt);
    }

    /**
     * Очистить кэш
     */
    public void clear() {
        cache.clear();
        log.info("Rates cache cleared");
    }

    /**
     * Очистить устаревшие записи
     */
    public void evictExpired() {
        cache.entrySet().removeIf(entry -> 
            Instant.now().isAfter(entry.getValue().expiresAt())
        );
    }

    /**
     * Запись в кэше
     */
    private record CachedRates(
        ExchangeRatesResult result,
        boolean isCrypto,
        Instant expiresAt
    ) {}
}
