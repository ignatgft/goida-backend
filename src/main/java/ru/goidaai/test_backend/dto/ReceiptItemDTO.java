package ru.goidaai.test_backend.dto;

import java.math.BigDecimal;

public record ReceiptItemDTO(
    String name,
    String title,
    BigDecimal quantity,
    BigDecimal price,
    BigDecimal unitPrice,
    BigDecimal totalPrice
) {
}
