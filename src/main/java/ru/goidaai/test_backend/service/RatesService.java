package ru.goidaai.test_backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.goidaai.test_backend.dto.CryptoRatesResponse;
import ru.goidaai.test_backend.dto.FiatRatesResponse;
import ru.goidaai.test_backend.service.rates.ExchangeRatesResult;
import ru.goidaai.test_backend.service.rates.CryptoExchangeRateProvider;
import ru.goidaai.test_backend.service.rates.FiatExchangeRateProvider;
import ru.goidaai.test_backend.service.rates.RatesCache;

/**
 * Сервис для получения курсов валют
 * Использует внешние API с кэшированием и fallback на резервные данные
 */
@Service
public class RatesService {

    private static final Logger log = LoggerFactory.getLogger(RatesService.class);

    private final FiatExchangeRateProvider fiatProvider;
    private final CryptoExchangeRateProvider cryptoProvider;
    private final RatesCache cache;
    private final Clock clock;

    public RatesService(
        FiatExchangeRateProvider fiatProvider,
        CryptoExchangeRateProvider cryptoProvider,
        RatesCache cache,
        Clock clock
    ) {
        this.fiatProvider = fiatProvider;
        this.cryptoProvider = cryptoProvider;
        this.cache = cache;
        this.clock = clock;
    }

    /**
     * Получить курсы фиатных валют
     */
    @Transactional(readOnly = true)
    public FiatRatesResponse getFiatRates(String baseCurrency) {
        String cacheKey = "fiat:" + baseCurrency.toUpperCase();
        
        // Проверяем кэш
        ExchangeRatesResult cached = cache.get(cacheKey);
        if (cached != null) {
            return FiatRatesResponse.from(cached);
        }

        // Получаем от провайдера
        ExchangeRatesResult result = fiatProvider.getRates(baseCurrency);
        cache.put(cacheKey, result, false);

        return FiatRatesResponse.from(result);
    }

    /**
     * Получить курсы криптовалют
     */
    @Transactional(readOnly = true)
    public CryptoRatesResponse getCryptoRates(String quoteCurrency) {
        String cacheKey = "crypto:" + quoteCurrency.toUpperCase();
        
        // Проверяем кэш
        ExchangeRatesResult cached = cache.get(cacheKey);
        if (cached != null) {
            return CryptoRatesResponse.from(cached);
        }

        // Получаем от провайдера (в USD)
        ExchangeRatesResult usdResult = cryptoProvider.getRates("USD");
        
        // Если нужна не USD валюта, конвертируем
        ExchangeRatesResult result;
        if ("USD".equalsIgnoreCase(quoteCurrency)) {
            result = usdResult;
        } else {
            // Получаем курс USD к нужной валюте
            ExchangeRatesResult fiatResult = fiatProvider.getRates(quoteCurrency);
            BigDecimal usdRate = fiatResult.rates().getOrDefault("USD", BigDecimal.ONE);
            
            // Конвертируем крипто курсы
            Map<String, BigDecimal> convertedRates = usdResult.rates().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().multiply(usdRate, new java.math.MathContext(10))
                ));
            
            result = new ExchangeRatesResult(
                quoteCurrency.toUpperCase(),
                convertedRates,
                clock.instant(),
                usdResult.source()
            );
        }
        
        cache.put(cacheKey, result, true);
        return CryptoRatesResponse.from(result);
    }

    /**
     * Конвертировать сумму из одной валюты в другую
     * @param amount Сумма
     * @param fromCurrency Из валюты
     * @param toCurrency В валюту
     * @return Конвертированная сумма
     */
    @Transactional(readOnly = true)
    public BigDecimal convertAmount(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (amount == null || BigDecimal.ZERO.compareTo(amount) == 0) {
            return BigDecimal.ZERO;
        }

        String from = fromCurrency.toUpperCase();
        String to = toCurrency.toUpperCase();

        // Одинаковые валюты
        if (from.equals(to)) {
            return amount;
        }

        // Обе фиатные
        if (isFiat(from) && isFiat(to)) {
            return convertFiatToFiat(amount, from, to);
        }

        // Обе крипто
        if (isCrypto(from) && isCrypto(to)) {
            return convertCryptoToCrypto(amount, from, to);
        }

        // Фиат в крипту
        if (isFiat(from) && isCrypto(to)) {
            return convertFiatToCrypto(amount, from, to);
        }

        // Крипта в фиат
        if (isCrypto(from) && isFiat(to)) {
            return convertCryptoToFiat(amount, from, to);
        }

        // По умолчанию возвращаем как есть
        log.warn("Unknown conversion from {} to {}, returning original amount", from, to);
        return amount;
    }

    /**
     * Проверить является ли валюта фиатной
     */
    public boolean isFiat(String currency) {
        // Простая эвристика: фиатные валюты 2-3 символа
        return currency != null && currency.length() <= 4 && !isCrypto(currency);
    }

    /**
     * Проверить является ли валюту криптой
     */
    public boolean isCrypto(String currency) {
        if (currency == null) return false;
        return CryptoExchangeRateProvider.SYMBOL_TO_ID.containsKey(currency.toUpperCase());
    }

    // ========== Методы конвертации ==========

    /**
     * Конвертация фиата в фиат через кросс-курс
     */
    private BigDecimal convertFiatToFiat(BigDecimal amount, String from, String to) {
        ExchangeRatesResult rates = fiatProvider.getRates(from);
        BigDecimal rate = rates.rates().get(to);
        
        if (rate == null) {
            // Пробуем через USD
            BigDecimal fromToUsd = getFiatToUsdRate(from);
            BigDecimal usdToTo = getUsdToFiatRate(to);
            rate = fromToUsd.multiply(usdToTo, new java.math.MathContext(10));
        }

        return amount.multiply(rate, new java.math.MathContext(10))
            .setScale(8, RoundingMode.HALF_UP);
    }

    /**
     * Конвертация крипты в крипту через USD
     */
    private BigDecimal convertCryptoToCrypto(BigDecimal amount, String from, String to) {
        // Конвертируем из крипты в USD
        BigDecimal amountInUsd = convertCryptoToFiat(amount, from, "USD");
        // Конвертируем из USD в крипту
        return convertFiatToCrypto(amountInUsd, "USD", to);
    }

    /**
     * Конвертация фиата в крипту
     */
    private BigDecimal convertFiatToCrypto(BigDecimal amount, String from, String to) {
        // Сначала в USD если нужно
        BigDecimal amountInUsd = from.equalsIgnoreCase("USD") 
            ? amount 
            : convertFiatToFiat(amount, from, "USD");

        // Получаем курс крипты в USD
        ExchangeRatesResult cryptoRates = cryptoProvider.getRates("USD");
        BigDecimal cryptoRate = cryptoRates.rates().get(to);

        if (cryptoRate == null || cryptoRate.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("No rate for crypto {}", to);
            return amount;
        }

        // amount_in_usd / crypto_rate = crypto_amount
        return amountInUsd.divide(cryptoRate, 8, RoundingMode.HALF_UP);
    }

    /**
     * Конвертация крипты в фиат
     */
    private BigDecimal convertCryptoToFiat(BigDecimal amount, String from, String to) {
        // Получаем курс крипты в USD
        ExchangeRatesResult cryptoRates = cryptoProvider.getRates("USD");
        BigDecimal cryptoRate = cryptoRates.rates().get(from);

        if (cryptoRate == null) {
            log.warn("No rate for crypto {}", from);
            return amount;
        }

        // amount * crypto_rate = amount_in_usd
        BigDecimal amountInUsd = amount.multiply(cryptoRate, new java.math.MathContext(10));

        // Если нужен USD, возвращаем
        if (to.equalsIgnoreCase("USD")) {
            return amountInUsd.setScale(8, RoundingMode.HALF_UP);
        }

        // Конвертируем USD в нужную валюту
        return convertFiatToFiat(amountInUsd, "USD", to);
    }

    /**
     * Получить курс фиата к USD
     */
    private BigDecimal getFiatToUsdRate(String currency) {
        if ("USD".equalsIgnoreCase(currency)) {
            return BigDecimal.ONE;
        }
        
        ExchangeRatesResult rates = fiatProvider.getRates(currency);
        // Курс это сколько единиц валюты за 1 единицу base
        // Нам нужно обратное
        BigDecimal rate = rates.rates().get("USD");
        return rate != null ? rate : BigDecimal.ONE;
    }

    /**
     * Получить курс USD к фиату
     */
    private BigDecimal getUsdToFiatRate(String currency) {
        if ("USD".equalsIgnoreCase(currency)) {
            return BigDecimal.ONE;
        }
        
        ExchangeRatesResult rates = fiatProvider.getRates("USD");
        BigDecimal rate = rates.rates().get(currency);
        return rate != null ? rate : BigDecimal.ONE;
    }

    /**
     * Получить текущий курс криптовалюты в базовой валюте пользователя
     */
    @Transactional(readOnly = true)
    public BigDecimal getCryptoPriceInCurrency(String cryptoSymbol, String currency) {
        if (!isCrypto(cryptoSymbol)) {
            return BigDecimal.ZERO;
        }

        return convertCryptoToFiat(BigDecimal.ONE, cryptoSymbol.toUpperCase(), currency);
    }

    /**
     * Рассчитать текущую стоимость актива в базовой валюте
     * @param asset Актив
     * @param baseCurrency Базовая валюта пользователя
     * @return Стоимость в базовой валюте
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateAssetCurrentValue(
        ru.goidaai.test_backend.model.Asset asset,
        String baseCurrency
    ) {
        if (asset == null || asset.getBalance() == null || asset.getBalance().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        String assetCurrency = asset.getSymbol();
        BigDecimal balance = asset.getBalance();

        // Если валюта актива совпадает с базовой
        if (assetCurrency.equalsIgnoreCase(baseCurrency)) {
            return balance.setScale(2, RoundingMode.HALF_UP);
        }

        // Конвертируем баланс актива в базовую валюту
        BigDecimal convertedAmount = convertAmount(balance, assetCurrency, baseCurrency);
        return convertedAmount.setScale(2, RoundingMode.HALF_UP);
    }
}
