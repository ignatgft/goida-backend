package ru.goidaai.test_backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import ru.goidaai.test_backend.model.enums.TransactionKind;
import ru.goidaai.test_backend.model.enums.TransactionSourceType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateTransactionRequest(
    @NotBlank(message = "category is required")
    String category,
    @JsonAlias("type")
    @NotNull(message = "kind is required")
    TransactionKind kind,
    String title,
    BigDecimal amount,
    String currency,
    @JsonAlias({"createdAt", "date", "timestamp"})
    Instant occurredAt,
    String sourceAssetId,
    String sourceAssetName,
    String note,
    TransactionSourceType sourceType,
    String processedReceiptId,
    ReceiptMetadataRequest receipt
) {
}
