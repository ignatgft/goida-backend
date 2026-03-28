package ru.goidaai.test_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import ru.goidaai.test_backend.model.enums.TransactionKind;
import ru.goidaai.test_backend.model.enums.TransactionSourceType;

public record UpdateTransactionRequest(
    @NotBlank(message = "title is required")
    String title,
    
    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    BigDecimal amount,
    
    @NotBlank(message = "currency is required")
    String currency,
    
    @NotBlank(message = "category is required")
    String category,
    
    @NotNull(message = "kind is required")
    TransactionKind kind,
    
    @NotNull(message = "occurredAt is required")
    Instant occurredAt,
    
    String note,
    String sourceAssetId,
    String sourceAssetName,
    
    TransactionSourceType sourceType,
    ReceiptMetadataRequest receipt
) {
}
