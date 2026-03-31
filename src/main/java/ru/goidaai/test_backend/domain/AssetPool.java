package ru.goidaai.test_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Доменная модель - Пул активов
 * Представляет актив с несколькими валютами (пул)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetPool {
    private String id;
    private String userId;
    private String name;
    private AssetPoolType type;
    private String baseCurrency;
    private BigDecimal totalBalance;
    private BigDecimal totalValueInBaseCurrency;
    private String note;
    private Instant createdAt;
    private Instant updatedAt;
}
