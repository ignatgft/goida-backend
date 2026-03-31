package ru.goidaai.test_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Доменная модель - Элемент пула (отдельная валюта в пуле)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoolItem {
    private String id;
    private String poolId;
    private String currency;
    private BigDecimal balance;
    private BigDecimal valueInBaseCurrency;
    private Double weight; // Процент от общего пула
    private Instant createdAt;
    private Instant updatedAt;
}
