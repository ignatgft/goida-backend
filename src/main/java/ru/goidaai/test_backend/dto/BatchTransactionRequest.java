package ru.goidaai.test_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.goidaai.test_backend.dto.transaction.TransactionCreateRequest;

import java.util.List;

/**
 * DTO для батч-создания транзакций
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTransactionRequest {
    private List<TransactionCreateRequest> transactions;
}
