package ru.goidaai.test_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Доменная модель - Элемент пула (отдельная валюта в пуле)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoolItem {
    private String id;
    private String poolId;
    private String currency;
    private BigDecimal balance;
    private BigDecimal valueInBaseCurrency;
    private Double weight;
    private Instant createdAt;
    private Instant updatedAt;
}
