package ru.goidaai.test_backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReceiptItemRequest(
    @JsonAlias("title")
    @NotBlank(message = "item name is required")
    String name,
    BigDecimal quantity,
    @JsonAlias("price")
    BigDecimal unitPrice,
    BigDecimal totalPrice
) {
}
