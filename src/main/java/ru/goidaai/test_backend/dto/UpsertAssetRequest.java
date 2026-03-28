package ru.goidaai.test_backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import ru.goidaai.test_backend.model.enums.AssetType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpsertAssetRequest(
    @NotBlank(message = "name is required")
    String name,
    @NotNull(message = "type is required")
    AssetType type,
    @JsonAlias("currency")
    @NotBlank(message = "symbol is required")
    String symbol,
    @JsonAlias("amount")
    @NotNull(message = "balance is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "balance must be greater than or equal to zero")
    BigDecimal balance,
    String note
) {
}
