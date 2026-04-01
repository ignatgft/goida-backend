package ru.goidaai.test_backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import ru.goidaai.test_backend.model.enums.AuthProvider;

public record UserDTO(
    String id,
    String email,
    String fullName,
    String avatarUrl,
    String baseCurrency,
    BigDecimal monthlyBudget,
    AuthProvider authProvider,
    boolean emailVerified,
    Instant lastLoginAt,
    Instant createdAt,
    Instant updatedAt,
    String language,
    String theme,
    Boolean emailNotifications,
    Boolean pushNotifications,
    String timezone
) {
}
