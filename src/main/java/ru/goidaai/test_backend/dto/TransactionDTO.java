package ru.goidaai.test_backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import ru.goidaai.test_backend.model.enums.TransactionKind;
import ru.goidaai.test_backend.model.enums.TransactionSourceType;

public record TransactionDTO(
    String id,
    String title,
    String category,
    String type,
    TransactionKind kind,
    TransactionSourceType sourceType,
    BigDecimal amount,
    String currency,
    Instant occurredAt,
    Instant createdAt,
    String sourceAssetId,
    String sourceAssetName,
    String note,
    ReceiptDTO receipt
) {
}
