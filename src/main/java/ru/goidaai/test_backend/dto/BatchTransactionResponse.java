package ru.goidaai.test_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.goidaai.test_backend.dto.transaction.TransactionResponse;

import java.util.List;

/**
 * DTO для ответа батч-операции
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTransactionResponse {
    private List<TransactionResponse> successful;
    private List<BatchError> errors;
    private int totalProcessed;
    private int successCount;
    private int errorCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchError {
        private int index;
        private String error;
        private TransactionCreateRequest request;
    }
}
