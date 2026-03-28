package ru.goidaai.test_backend.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO актива
 * Примечание: currency и symbol - одно и то же (код валюты)
 * amount и balance - одно и то же (баланс актива)
 * Оставлены для обратной совместимости с фронтендом
 */
public record AssetDTO(
    String id,
    String name,
    String type,
    String currency,        // Код валюты (USD, EUR, etc.)
    BigDecimal amount,      // Баланс актива
    String symbol,          // Синоним currency для совместимости
    BigDecimal balance,     // Синоним amount для совместимости
    BigDecimal currentValue, // Текущая стоимость в базовой валюте
    String baseCurrency,
    String note,
    Instant createdAt,
    Instant updatedAt
) {
}
