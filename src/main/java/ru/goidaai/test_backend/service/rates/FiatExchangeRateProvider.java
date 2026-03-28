package ru.goidaai.test_backend.service.rates;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Провайдер курсов фиатных валют через ExchangeRate-API
 * Бесплатный API: https://www.exchangerate-api.com/
 * Альтернатива: Fixer.io, Open Exchange Rates
 */
@Component
public class FiatExchangeRateProvider implements ExchangeRateProvider {

    private static final Logger log = LoggerFactory.getLogger(FiatExchangeRateProvider.class);
    private static final String API_URL = "https://open.er-api.com/v6/latest/%s";
    
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public FiatExchangeRateProvider(
        @Value("${EXCHANGE_API_KEY:}") String apiKey
    ) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getName() {
        return "ExchangeRate-API";
    }

    @Override
    public boolean supportsFiat() {
        return true;
    }

    @Override
    public boolean supportsCrypto() {
        return false;
    }

    @Override
    public ExchangeRatesResult getRates(String baseCurrency) {
        String url = String.format(API_URL, baseCurrency.toUpperCase());
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.warn("ExchangeRate-API returned status {}: {}", response.statusCode(), response.body());
                return getFallbackRates(baseCurrency);
            }

            JsonNode root = objectMapper.readTree(response.body());
            
            if (!root.get("result").asText().equals("success")) {
                log.warn("ExchangeRate-API error: {}", response.body());
                return getFallbackRates(baseCurrency);
            }

            Map<String, BigDecimal> rates = parseRates(root.get("rates"));
            
            return new ExchangeRatesResult(
                baseCurrency.toUpperCase(),
                rates,
                Instant.now(),
                getName()
            );

        } catch (IOException | InterruptedException e) {
            log.error("Error fetching rates from ExchangeRate-API", e);
            Thread.currentThread().interrupt();
            return getFallbackRates(baseCurrency);
        }
    }

    @Override
    public int getPriority() {
        return 10; // Высокий приоритет для фиата
    }

    /**
     * Парсинг курсов из JSON
     */
    private Map<String, BigDecimal> parseRates(JsonNode ratesNode) {
        Map<String, BigDecimal> rates = new HashMap<>();
        
        if (ratesNode == null || !ratesNode.isObject()) {
            return getFallbackRatesMap();
        }

        ratesNode.fields().forEachRemaining(entry -> {
            String currency = entry.getKey();
            BigDecimal rate = entry.getValue().decimalValue();
            rates.put(currency, rate.setScale(6, BigDecimal.ROUND_HALF_UP));
        });

        return rates;
    }

    /**
     * Резервные курсы при ошибке API
     */
    private ExchangeRatesResult getFallbackRates(String baseCurrency) {
        return new ExchangeRatesResult(
            baseCurrency.toUpperCase(),
            getFallbackRatesMap(),
            Instant.now(),
            "fallback"
        );
    }

    private Map<String, BigDecimal> getFallbackRatesMap() {
        // Базовые курсы USD (обновляются редко)
        Map<String, BigDecimal> fallback = new HashMap<>();
        fallback.put("USD", BigDecimal.ONE);
        fallback.put("EUR", new BigDecimal("0.92"));
        fallback.put("GBP", new BigDecimal("0.79"));
        fallback.put("RUB", new BigDecimal("92.50"));
        fallback.put("KZT", new BigDecimal("450.00"));
        fallback.put("CNY", new BigDecimal("7.20"));
        fallback.put("JPY", new BigDecimal("150.00"));
        fallback.put("CHF", new BigDecimal("0.88"));
        fallback.put("CAD", new BigDecimal("1.35"));
        fallback.put("AUD", new BigDecimal("1.52"));
        fallback.put("NZD", new BigDecimal("1.64"));
        fallback.put("SGD", new BigDecimal("1.34"));
        fallback.put("HKD", new BigDecimal("7.82"));
        fallback.put("SEK", new BigDecimal("10.40"));
        fallback.put("NOK", new BigDecimal("10.60"));
        fallback.put("DKK", new BigDecimal("6.85"));
        fallback.put("PLN", new BigDecimal("3.95"));
        fallback.put("CZK", new BigDecimal("23.00"));
        fallback.put("HUF", new BigDecimal("360.00"));
        fallback.put("TRY", new BigDecimal("32.00"));
        fallback.put("AED", new BigDecimal("3.67"));
        fallback.put("SAR", new BigDecimal("3.75"));
        fallback.put("INR", new BigDecimal("83.00"));
        fallback.put("BRL", new BigDecimal("4.95"));
        fallback.put("MXN", new BigDecimal("17.00"));
        fallback.put("ZAR", new BigDecimal("18.50"));
        fallback.put("KRW", new BigDecimal("1330.00"));
        fallback.put("THB", new BigDecimal("35.50"));
        fallback.put("MYR", new BigDecimal("4.70"));
        fallback.put("IDR", new BigDecimal("15600.00"));
        fallback.put("PHP", new BigDecimal("56.00"));
        fallback.put("VND", new BigDecimal("24500.00"));
        fallback.put("UAH", new BigDecimal("39.00"));
        fallback.put("GEL", new BigDecimal("2.70"));
        fallback.put("AMD", new BigDecimal("390.00"));
        fallback.put("AZN", new BigDecimal("1.70"));
        fallback.put("UZS", new BigDecimal("12500.00"));
        fallback.put("MDL", new BigDecimal("17.50"));
        fallback.put("RSD", new BigDecimal("107.00"));
        fallback.put("BYN", new BigDecimal("3.25"));
        fallback.put("KGS", new BigDecimal("89.00"));
        fallback.put("TJS", new BigDecimal("10.90"));
        fallback.put("TMT", new BigDecimal("3.50"));
        return fallback;
    }
}
