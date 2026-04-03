package ru.goidaai.test_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для ответа батч-операции
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTransactionResponse {
    private List<TransactionDTO> successful;
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
        private CreateTransactionRequest request;
    }
}
