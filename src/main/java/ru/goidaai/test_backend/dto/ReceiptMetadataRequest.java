package ru.goidaai.test_backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReceiptMetadataRequest(
    String merchant,
    BigDecimal total,
    String currency,
    @JsonAlias({"createdAt", "date"})
    Instant purchasedAt,
    List<ReceiptItemRequest> items
) {
}
