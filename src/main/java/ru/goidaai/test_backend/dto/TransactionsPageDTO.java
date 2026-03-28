package ru.goidaai.test_backend.dto;

import java.util.List;

public record TransactionsPageDTO(
    List<TransactionDTO> items,
    String nextCursor,
    boolean hasMore
) {
}
