package ru.goidaai.test_backend.dto;

import java.math.BigDecimal;

/**
 * DTO для отображения общего баланса активов и потраченного баланса
 */
public record AssetBalanceSummaryDTO(
    BigDecimal totalAssets,      // Общий баланс всех активов в базовой валюте
    BigDecimal spentBalance,     // Потраченный баланс (расходы за период)
    String baseCurrency,         // Базовая валюта
    String periodLabel           // Период (за который считаются расходы)
) {
}
