package ru.goidaai.test_backend.dto;

import java.time.Instant;

public record ChatMessageDTO(
    String text,
    String role,
    Instant timestamp,
    String message,
    String response,
    String provider,
    String model,
    Instant createdAt
) {
}
