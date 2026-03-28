package ru.goidaai.test_backend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

public enum TransactionKind {
    EXPENSE,
    INCOME,
    TRANSFER;

    @JsonCreator
    public static TransactionKind fromValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        return TransactionKind.valueOf(rawValue.trim().toUpperCase(Locale.ROOT));
    }

    public String clientValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
