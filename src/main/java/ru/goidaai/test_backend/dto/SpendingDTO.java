package ru.goidaai.test_backend.dto;

import java.math.BigDecimal;

public record SpendingDTO(
    BigDecimal spent,
    BigDecimal budget
) {
}
