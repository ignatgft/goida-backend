package ru.goidaai.test_backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
    @NotBlank(message = "message is required")
    String message,
    String locale
) {
}
