package ru.goidaai.test_backend.controller;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.goidaai.test_backend.dto.SendMoneyRequest;
import ru.goidaai.test_backend.dto.TopUpRequest;
import ru.goidaai.test_backend.dto.TransactionDTO;
import ru.goidaai.test_backend.service.TransactionsService;

/**
 * Контроллер для быстрых операций (переводы, пополнения)
 * TODO: Реализовать функционал
 */
@RestController
@RequestMapping("/api")
public class QuickActionsController {

    private final TransactionsService transactionsService;

    public QuickActionsController(TransactionsService transactionsService) {
        this.transactionsService = transactionsService;
    }

    @PostMapping("/send")
    public ResponseEntity<TransactionDTO> send(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody SendMoneyRequest request
    ) {
        // TODO: Реализовать переводы
        TransactionDTO transaction = transactionsService.send(
            jwt.getSubject(),
            request.recipient(),
            request.amount()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/topup")
    public ResponseEntity<TransactionDTO> topUp(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody TopUpRequest request
    ) {
        // TODO: Реализовать пополнения
        TransactionDTO transaction = transactionsService.topUp(
            jwt.getSubject(),
            request.amount()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
}
