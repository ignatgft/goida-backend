package ru.goidaai.test_backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ReceiptDTO(
    String id,
    String receiptId,
    String merchant,
    BigDecimal total,
    String currency,
    Instant purchasedAt,
    List<ReceiptItemDTO> items,
    String imageUrl,
    Instant createdAt
) {
}
