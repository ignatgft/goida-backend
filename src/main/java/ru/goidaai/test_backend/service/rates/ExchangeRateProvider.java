package ru.goidaai.test_backend.service.rates;

/**
 * Интерфейс для провайдеров курсов валют
 */
public interface ExchangeRateProvider {
    
    /**
     * Получить название провайдера
     */
    String getName();
    
    /**
     * Поддерживает ли провайдер фиатные валюты
     */
    boolean supportsFiat();
    
    /**
     * Поддерживает ли провайдер криптовалюты
     */
    boolean supportsCrypto();
    
    /**
     * Получить курсы валют
     * @param baseCurrency Базовая валюта
     * @return Результат с курсами
     */
    ExchangeRatesResult getRates(String baseCurrency);
    
    /**
     * Приоритет провайдера (меньше = выше приоритет)
     */
    default int getPriority() {
        return 100;
    }
}
