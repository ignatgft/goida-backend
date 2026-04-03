package ru.goidaai.test_backend.adapter.in.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.goidaai.test_backend.dto.BatchTransactionRequest;
import ru.goidaai.test_backend.dto.BatchTransactionResponse;
import ru.goidaai.test_backend.dto.CreateTransactionRequest;
import ru.goidaai.test_backend.dto.TransactionDTO;
import ru.goidaai.test_backend.dto.TransactionsPageDTO;
import ru.goidaai.test_backend.service.TransactionsService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionsController {

    private final TransactionsService transactionsService;

    public TransactionsController(TransactionsService transactionsService) {
        this.transactionsService = transactionsService;
    }

    @GetMapping
    public TransactionsPageDTO list(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "month") String period,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) String cursor
    ) {
        return transactionsService.list(jwt.getSubject(), category, period, limit, cursor);
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> create(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody CreateTransactionRequest request
    ) {
        TransactionDTO transaction = transactionsService.create(jwt.getSubject(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    /**
     * Batch создание транзакций
     * Позволяет создать несколько транзакций за один запрос
     * Каждая транзакция обрабатывается индивидуально - успех/ошибка отдельно
     */
    @PostMapping("/batch")
    public ResponseEntity<BatchTransactionResponse> createBatch(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody BatchTransactionRequest batchRequest
    ) {
        String userId = jwt.getSubject();
        List<TransactionDTO> successful = new ArrayList<>();
        List<BatchTransactionResponse.BatchError> errors = new ArrayList<>();

        for (int i = 0; i < batchRequest.getTransactions().size(); i++) {
            CreateTransactionRequest request = batchRequest.getTransactions().get(i);
            try {
                // Реально вызываем сервис для создания транзакции
                TransactionDTO transaction = transactionsService.create(userId, request);
                successful.add(transaction);
            } catch (Exception e) {
                errors.add(BatchTransactionResponse.BatchError.builder()
                    .index(i)
                    .error(e.getMessage())
                    .request(request)
                    .build());
            }
        }

        BatchTransactionResponse response = BatchTransactionResponse.builder()
            .successful(successful)
            .errors(errors)
            .totalProcessed(batchRequest.getTransactions().size())
            .successCount(successful.size())
            .errorCount(errors.size())
            .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<TransactionDTO> update(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String transactionId,
        @Valid @RequestBody CreateTransactionRequest request
    ) {
        TransactionDTO transaction = transactionsService.update(jwt.getSubject(), transactionId, request);
        return ResponseEntity.ok(transaction);
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String transactionId
    ) {
        transactionsService.delete(jwt.getSubject(), transactionId);
        return ResponseEntity.noContent().build();
    }
}
