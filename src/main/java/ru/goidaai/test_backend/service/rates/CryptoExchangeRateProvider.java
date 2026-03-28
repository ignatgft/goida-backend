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
import org.springframework.stereotype.Component;

/**
 * Провайдер курсов криптовалют через CoinGecko API
 * Бесплатный API: https://www.coingecko.com/
 * Документация: https://www.coingecko.com/api/documentation
 */
@Component
public class CryptoExchangeRateProvider implements ExchangeRateProvider {

    private static final Logger log = LoggerFactory.getLogger(CryptoExchangeRateProvider.class);
    private static final String API_URL = "https://api.coingecko.com/api/v3/simple/price";
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Популярные криптовалюты для отслеживания
    private static final String[] CRYPTO_IDS = {
        "bitcoin", "ethereum", "tether", "usd-coin", "binancecoin", 
        "ripple", "solana", "cardano", "dogecoin", "tron", "toncoin",
        "avalanche-2", "polkadot", "chainlink", "bitcoin-cash", "litecoin",
        "stellar", "ethereum-classic", "cosmos", "monero", "near", "aptos",
        "sui", "optimism", "arbitrum", "injective-protocol", "shiba-inu",
        "filecoin", "hedera-hashgraph", "internet-computer", "vechain",
        "algorand", "aave", "maker", "eos", "the-sandbox", "decentraland",
        "uniswap", "jupiter", "kaspa", "bittensor", "cronos", "pepe",
        "dogwifhat", "bonk", "stacks", "theta-token", "gala", "flow",
        "tezos", "zcash", "compound", "synthetix-network-token", "jito-governance-token",
        "celestia", "kucoin-shares", "quant-network"
    };

    // Маппинг символов на ID CoinGecko - публичный для доступа из RatesService
    public static final Map<String, String> SYMBOL_TO_ID = Map.ofEntries(
        Map.entry("BTC", "bitcoin"),
        Map.entry("ETH", "ethereum"),
        Map.entry("USDT", "tether"),
        Map.entry("USDC", "usd-coin"),
        Map.entry("BNB", "binancecoin"),
        Map.entry("XRP", "ripple"),
        Map.entry("SOL", "solana"),
        Map.entry("ADA", "cardano"),
        Map.entry("DOGE", "dogecoin"),
        Map.entry("TRX", "tron"),
        Map.entry("TON", "the-open-network"),
        Map.entry("AVAX", "avalanche-2"),
        Map.entry("DOT", "polkadot"),
        Map.entry("LINK", "chainlink"),
        Map.entry("BCH", "bitcoin-cash"),
        Map.entry("LTC", "litecoin"),
        Map.entry("XLM", "stellar"),
        Map.entry("ETC", "ethereum-classic"),
        Map.entry("ATOM", "cosmos"),
        Map.entry("XMR", "monero"),
        Map.entry("NEAR", "near"),
        Map.entry("APT", "aptos"),
        Map.entry("SUI", "sui"),
        Map.entry("OP", "optimism"),
        Map.entry("ARB", "arbitrum"),
        Map.entry("INJ", "injective-protocol"),
        Map.entry("SHIB", "shiba-inu"),
        Map.entry("FIL", "filecoin"),
        Map.entry("HBAR", "hedera-hashgraph"),
        Map.entry("ICP", "internet-computer"),
        Map.entry("VET", "vechain"),
        Map.entry("ALGO", "algorand"),
        Map.entry("AAVE", "aave"),
        Map.entry("MKR", "maker"),
        Map.entry("EOS", "eos"),
        Map.entry("SAND", "the-sandbox"),
        Map.entry("MANA", "decentraland"),
        Map.entry("UNI", "uniswap"),
        Map.entry("JUP", "jupiter"),
        Map.entry("KAS", "kaspa"),
        Map.entry("TAO", "bittensor"),
        Map.entry("CRO", "cronos"),
        Map.entry("PEPE", "pepe"),
        Map.entry("WIF", "dogwifhat"),
        Map.entry("BONK", "bonk"),
        Map.entry("STX", "stacks"),
        Map.entry("THETA", "theta-token"),
        Map.entry("GALA", "gala"),
        Map.entry("FLOW", "flow"),
        Map.entry("XTZ", "tezos"),
        Map.entry("ZEC", "zcash"),
        Map.entry("COMP", "compound"),
        Map.entry("SNX", "synthetix-network-token"),
        Map.entry("JTO", "jito-governance-token"),
        Map.entry("TIA", "celestia"),
        Map.entry("KCS", "kucoin-shares"),
        Map.entry("QNT", "quant-network")
    );

    public CryptoExchangeRateProvider() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getName() {
        return "CoinGecko";
    }

    @Override
    public boolean supportsFiat() {
        return false;
    }

    @Override
    public boolean supportsCrypto() {
        return true;
    }

    @Override
    public ExchangeRatesResult getRates(String baseCurrency) {
        String vsCurrency = baseCurrency.toLowerCase();
        
        // CoinGecko имеет строгие rate limits: 10-30 запросов в минуту
        // Используем только один запрос с топ-30 монетами
        String[] priorityIds = {
            "bitcoin", "ethereum", "tether", "usd-coin", "binancecoin",
            "ripple", "solana", "cardano", "dogecoin", "tron",
            "the-open-network", "avalanche-2", "polkadot", "chainlink", "bitcoin-cash",
            "litecoin", "stellar", "ethereum-classic", "cosmos", "monero",
            "near", "aptos", "sui", "optimism", "arbitrum",
            "injective-protocol", "shiba-inu", "filecoin", "hedera-hashgraph", "internet-computer"
        };
        
        String ids = String.join(",", priorityIds);
        
        try {
            String url = String.format("%s?ids=%s&vs_currencies=%s", API_URL, ids, vsCurrency);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.warn("CoinGecko returned status {}: {}", response.statusCode(), response.body());
                return getFallbackRates(baseCurrency);
            }

            JsonNode root = objectMapper.readTree(response.body());
            
            // Проверяем есть ли ошибка в ответе
            if (root.has("status") && !root.has("bitcoin")) {
                log.warn("CoinGecko returned error: {}", root.get("status"));
                return getFallbackRates(baseCurrency);
            }
            
            Map<String, BigDecimal> rates = parseRates(root);
            
            return new ExchangeRatesResult(
                baseCurrency.toUpperCase(),
                rates,
                Instant.now(),
                getName()
            );

        } catch (IOException | InterruptedException e) {
            log.error("Error fetching crypto rates from CoinGecko", e);
            Thread.currentThread().interrupt();
            return getFallbackRates(baseCurrency);
        }
    }

    @Override
    public int getPriority() {
        return 20; // Приоритет для крипто
    }

    /**
     * Парсинг курсов криптовалют из JSON
     */
    private Map<String, BigDecimal> parseRates(JsonNode root) {
        Map<String, BigDecimal> rates = new HashMap<>();
        
        if (root == null || !root.isObject()) {
            log.warn("Empty or invalid root node for crypto rates");
            return getFallbackCryptoRatesMap();
        }

        log.debug("Parsing crypto rates from CoinGecko: {} coins available", root.size());

        // Конвертируем ID обратно в символы
        SYMBOL_TO_ID.forEach((symbol, id) -> {
            JsonNode priceNode = root.get(id);
            if (priceNode != null && priceNode.isObject()) {
                // CoinGecko возвращает в формате: {"bitcoin": {"usd": 50000}}
                JsonNode priceValue = priceNode.get("usd");
                if (priceValue == null) {
                    // Пробуем получить первое доступное значение
                    var field = priceNode.fields();
                    if (field.hasNext()) {
                        priceValue = field.next().getValue();
                    }
                }
                if (priceValue != null && priceValue.isNumber()) {
                    BigDecimal rate = priceValue.decimalValue();
                    rates.put(symbol, rate.setScale(8, BigDecimal.ROUND_HALF_UP));
                    log.trace("Parsed {} = {}", symbol, rate);
                }
            } else {
                log.trace("No price data for {} (ID: {})", symbol, id);
            }
        });

        log.debug("Parsed {} crypto rates", rates.size());
        return rates;
    }

    /**
     * Резервные курсы при ошибке API
     */
    private ExchangeRatesResult getFallbackRates(String baseCurrency) {
        return new ExchangeRatesResult(
            baseCurrency.toUpperCase(),
            getFallbackCryptoRatesMap(),
            Instant.now(),
            "fallback"
        );
    }

    private Map<String, BigDecimal> getFallbackCryptoRatesMap() {
        // Базовые курсы в USD (обновляются редко)
        Map<String, BigDecimal> fallback = new HashMap<>();
        fallback.put("BTC", new BigDecimal("67000.00"));
        fallback.put("ETH", new BigDecimal("3500.00"));
        fallback.put("USDT", new BigDecimal("1.00"));
        fallback.put("USDC", new BigDecimal("1.00"));
        fallback.put("BNB", new BigDecimal("600.00"));
        fallback.put("XRP", new BigDecimal("0.62"));
        fallback.put("SOL", new BigDecimal("160.00"));
        fallback.put("ADA", new BigDecimal("0.70"));
        fallback.put("DOGE", new BigDecimal("0.16"));
        fallback.put("TRX", new BigDecimal("0.12"));
        fallback.put("TON", new BigDecimal("6.00"));
        fallback.put("AVAX", new BigDecimal("40.00"));
        fallback.put("DOT", new BigDecimal("8.50"));
        fallback.put("LINK", new BigDecimal("18.00"));
        fallback.put("BCH", new BigDecimal("480.00"));
        fallback.put("LTC", new BigDecimal("80.00"));
        fallback.put("XLM", new BigDecimal("0.14"));
        fallback.put("ETC", new BigDecimal("30.00"));
        fallback.put("ATOM", new BigDecimal("12.00"));
        fallback.put("XMR", new BigDecimal("140.00"));
        fallback.put("NEAR", new BigDecimal("7.00"));
        fallback.put("APT", new BigDecimal("12.00"));
        fallback.put("SUI", new BigDecimal("1.80"));
        fallback.put("OP", new BigDecimal("3.20"));
        fallback.put("ARB", new BigDecimal("1.70"));
        fallback.put("INJ", new BigDecimal("32.00"));
        fallback.put("SHIB", new BigDecimal("0.000028"));
        fallback.put("FIL", new BigDecimal("8.50"));
        fallback.put("HBAR", new BigDecimal("0.11"));
        fallback.put("ICP", new BigDecimal("12.50"));
        fallback.put("VET", new BigDecimal("0.047"));
        fallback.put("ALGO", new BigDecimal("0.25"));
        fallback.put("AAVE", new BigDecimal("170.00"));
        fallback.put("MKR", new BigDecimal("2800.00"));
        fallback.put("EOS", new BigDecimal("1.10"));
        fallback.put("SAND", new BigDecimal("0.65"));
        fallback.put("MANA", new BigDecimal("0.70"));
        fallback.put("UNI", new BigDecimal("11.00"));
        fallback.put("JUP", new BigDecimal("1.20"));
        fallback.put("KAS", new BigDecimal("0.18"));
        fallback.put("TAO", new BigDecimal("550.00"));
        fallback.put("CRO", new BigDecimal("0.11"));
        fallback.put("PEPE", new BigDecimal("0.0000075"));
        fallback.put("WIF", new BigDecimal("3.50"));
        fallback.put("BONK", new BigDecimal("0.000035"));
        fallback.put("STX", new BigDecimal("2.50"));
        fallback.put("THETA", new BigDecimal("2.20"));
        fallback.put("GALA", new BigDecimal("0.055"));
        fallback.put("FLOW", new BigDecimal("1.10"));
        fallback.put("XTZ", new BigDecimal("1.15"));
        fallback.put("ZEC", new BigDecimal("45.00"));
        fallback.put("COMP", new BigDecimal("70.00"));
        fallback.put("SNX", new BigDecimal("4.50"));
        fallback.put("JTO", new BigDecimal("4.00"));
        fallback.put("TIA", new BigDecimal("11.00"));
        fallback.put("KCS", new BigDecimal("11.00"));
        fallback.put("QNT", new BigDecimal("115.00"));
        return fallback;
    }
}
