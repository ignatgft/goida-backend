package ru.goidaai.test_backend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

public enum AssetType {
    FIAT,
    CRYPTO,
    BANK_ACCOUNT,
    CASH,
    SAVINGS,
    INVESTMENTS,
    OTHER;

    @JsonCreator
    public static AssetType fromValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return OTHER;
        }

        String normalized = rawValue
            .trim()
            .replace('-', '_')
            .replace(' ', '_')
            .toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case "FIAT" -> FIAT;
            case "CRYPTO" -> CRYPTO;
            case "BANK_ACCOUNT" -> BANK_ACCOUNT;
            case "CASH" -> CASH;
            case "SAVINGS" -> SAVINGS;
            case "INVESTMENTS" -> INVESTMENTS;
            default -> OTHER;
        };
    }

    public String clientValue() {
        return switch (this) {
            case BANK_ACCOUNT -> "bank_account";
            case SAVINGS -> "savings";
            case INVESTMENTS -> "investments";
            case CRYPTO -> "crypto";
            case CASH, FIAT, OTHER -> "cash";
        };
    }
}
