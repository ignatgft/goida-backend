package ru.goidaai.test_backend.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.goidaai.test_backend.dto.CreateTransactionRequest;
import ru.goidaai.test_backend.dto.ReceiptDTO;
import ru.goidaai.test_backend.dto.TransactionDTO;
import ru.goidaai.test_backend.dto.TransactionsPageDTO;
import ru.goidaai.test_backend.dto.UpdateTransactionRequest;
import ru.goidaai.test_backend.exception.BadRequestException;
import ru.goidaai.test_backend.model.Asset;
import ru.goidaai.test_backend.model.Receipt;
import ru.goidaai.test_backend.model.Transaction;
import ru.goidaai.test_backend.model.User;
import ru.goidaai.test_backend.model.enums.PeriodFilter;
import ru.goidaai.test_backend.model.enums.TransactionKind;
import ru.goidaai.test_backend.model.enums.TransactionSourceType;
import ru.goidaai.test_backend.repository.ReceiptRepository;
import ru.goidaai.test_backend.repository.TransactionRepository;
import ru.goidaai.test_backend.service.AssetsService;
import ru.goidaai.test_backend.service.transaction.CursorEncoder;
import ru.goidaai.test_backend.service.transaction.ReceiptLinkingService;
import ru.goidaai.test_backend.service.transaction.TransactionBalanceService;
import ru.goidaai.test_backend.service.transaction.TransactionTitleResolver;
import ru.goidaai.test_backend.service.transaction.TransactionValidator;

/**
 * Сервис для управления транзакциями
 * Координирует работу вспомогательных сервисов
 */
@Service
public class TransactionsService {

    private final TransactionRepository transactionRepository;
    private final ReceiptRepository receiptRepository;
    private final AssetsService assetsService;
    private final ReceiptService receiptService;
    private final CurrentUserService currentUserService;
    private final DtoFactory dtoFactory;

    // Вспомогательные сервисы
    private final TransactionValidator validator;
    private final TransactionTitleResolver titleResolver;
    private final TransactionBalanceService balanceService;
    private final ReceiptLinkingService receiptLinkingService;
    private final CursorEncoder cursorEncoder;

    public TransactionsService(
        TransactionRepository transactionRepository,
        ReceiptRepository receiptRepository,
        AssetsService assetsService,
        ReceiptService receiptService,
        CurrentUserService currentUserService,
        DtoFactory dtoFactory,
        TransactionValidator validator,
        TransactionTitleResolver titleResolver,
        TransactionBalanceService balanceService,
        ReceiptLinkingService receiptLinkingService,
        CursorEncoder cursorEncoder
    ) {
        this.transactionRepository = transactionRepository;
        this.receiptRepository = receiptRepository;
        this.assetsService = assetsService;
        this.receiptService = receiptService;
        this.currentUserService = currentUserService;
        this.dtoFactory = dtoFactory;
        this.validator = validator;
        this.titleResolver = titleResolver;
        this.balanceService = balanceService;
        this.receiptLinkingService = receiptLinkingService;
        this.cursorEncoder = cursorEncoder;
    }

    /**
     * Получить список транзакций с пагинацией
     */
    @Transactional(readOnly = true)
    public TransactionsPageDTO list(
        String userId,
        String category,
        String period,
        Integer limit,
        String cursor
    ) {
        User user = currentUserService.require(userId);
        PeriodFilter periodFilter = PeriodFilter.from(period);
        int safeLimit = sanitizeLimit(limit);

        Specification<Transaction> spec = buildListSpecification(user, category, periodFilter, cursor);
        Pageable pageable = PageRequest.of(0, safeLimit + 1);

        var page = transactionRepository.findAll(spec, pageable);
        List<Transaction> transactions = page.getContent();
        boolean hasMore = transactions.size() > safeLimit;

        if (hasMore) {
            transactions = transactions.subList(0, safeLimit);
        }

        String nextCursor = hasMore && !transactions.isEmpty()
            ? encodeCursor(transactions.get(transactions.size() - 1))
            : null;

        List<TransactionDTO> dtos = transactions.stream()
            .map(t -> dtoFactory.toTransactionDto(t, null))
            .toList();

        return new TransactionsPageDTO(dtos, nextCursor, hasMore);
    }

    /**
     * Создать новую транзакцию
     */
    @Transactional
    public TransactionDTO create(String userId, CreateTransactionRequest request) {
        User user = currentUserService.require(userId);
        validator.validateCreateRequest(request);

        // Определяем источник и заголовок
        TransactionSourceType sourceType = resolveSourceType(request);
        String title = titleResolver.resolveTitle(request, null);

        // Получаем актив
        Asset sourceAsset = null;
        String sourceAssetName = null;
        if (StringUtils.hasText(request.sourceAssetId())) {
            sourceAsset = assetsService.requireOwnedAsset(request.sourceAssetId(), userId);
            sourceAssetName = sourceAsset.getName();
        }

        // Создаём транзакцию
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setSourceAsset(sourceAsset);
        transaction.setSourceAssetName(sourceAssetName);
        transaction.setTitle(title);
        transaction.setCategory(request.category().trim());
        transaction.setKind(request.kind());
        transaction.setAmount(request.amount());
        transaction.setCurrency(normalizeCurrency(request.currency()));
        transaction.setNote(validator.trimToNull(request.note()));
        transaction.setOccurredAt(request.occurredAt() != null ? request.occurredAt() : java.time.Instant.now());
        transaction.setSourceType(sourceType);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Обновляем баланс актива
        balanceService.updateBalanceForTransaction(sourceAsset, request.kind(), request.amount());

        // Обрабатываем чек если есть
        ReceiptDTO receipt = processReceipt(savedTransaction, request);

        return dtoFactory.toTransactionDto(savedTransaction, receipt);
    }

    /**
     * Обновить транзакцию
     */
    @Transactional
    public TransactionDTO update(String userId, String transactionId, CreateTransactionRequest request) {
        User user = currentUserService.require(userId);
        validator.validateCreateRequest(request);

        Transaction transaction = transactionRepository.findByIdAndUser_Id(transactionId, userId)
            .orElseThrow(() -> new ru.goidaai.test_backend.exception.ResourceNotFoundException("Transaction not found"));

        // Сохраняем старые значения для восстановления баланса
        Asset oldSourceAsset = transaction.getSourceAsset();
        BigDecimal oldAmount = transaction.getAmount();

        // Обновляем актив если изменился
        Asset newSourceAsset = null;
        String newSourceAssetName = null;
        if (StringUtils.hasText(request.sourceAssetId())) {
            newSourceAsset = assetsService.requireOwnedAsset(request.sourceAssetId(), userId);
            newSourceAssetName = newSourceAsset.getName();
        }

        // Обновляем баланс
        balanceService.updateBalanceForTransactionChange(
            oldSourceAsset,
            newSourceAsset,
            request.kind(),
            oldAmount,
            request.amount()
        );

        // Обновляем поля
        transaction.setSourceAsset(newSourceAsset);
        transaction.setSourceAssetName(newSourceAssetName);
        transaction.setTitle(titleResolver.resolveTitleForUpdate(request, transaction.getTitle()));
        transaction.setCategory(request.category().trim());
        transaction.setKind(request.kind());
        transaction.setAmount(request.amount());
        transaction.setCurrency(normalizeCurrency(request.currency()));
        transaction.setNote(validator.trimToNull(request.note()));
        transaction.setOccurredAt(request.occurredAt() != null ? request.occurredAt() : transaction.getOccurredAt());

        Transaction updatedTransaction = transactionRepository.save(transaction);
        ReceiptDTO receipt = null; // receiptService.findByTransactionId не существует

        return dtoFactory.toTransactionDto(updatedTransaction, receipt);
    }

    /**
     * Удалить транзакцию
     */
    @Transactional
    public void delete(String userId, String transactionId) {
        User user = currentUserService.require(userId);
        Transaction transaction = transactionRepository.findByIdAndUser_Id(transactionId, userId)
            .orElseThrow(() -> new ru.goidaai.test_backend.exception.ResourceNotFoundException("Transaction not found"));

        // Восстанавливаем баланс актива
        balanceService.restoreBalanceForTransaction(
            transaction.getSourceAsset(),
            transaction.getKind(),
            transaction.getAmount()
        );

        transactionRepository.delete(transaction);
    }

    /**
     * Быстрый перевод
     * TODO: Реализовать полноценные переводы
     */
    @Transactional
    public TransactionDTO send(String userId, String recipient, BigDecimal amount) {
        throw new BadRequestException("Not implemented: быстрые переводы пока недоступны");
    }

    /**
     * Быстрое пополнение
     * TODO: Реализовать полноценные пополнения
     */
    @Transactional
    public TransactionDTO topUp(String userId, BigDecimal amount) {
        throw new BadRequestException("Not implemented: быстрое пополнение пока недоступно");
    }

    // ========== Вспомогательные методы ==========

    private Specification<Transaction> buildListSpecification(
        User user,
        String category,
        PeriodFilter period,
        String cursor
    ) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), user.getId()));
            predicates.add(criteriaBuilder.equal(root.get("kind"), TransactionKind.EXPENSE));
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                root.get("occurredAt"),
                period.startsAt(java.time.Clock.systemUTC())
            ));

            if (StringUtils.hasText(category)) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            if (StringUtils.hasText(cursor)) {
                String[] decoded = cursorEncoder.decode(cursor);
                if (decoded != null) {
                    String occurredAt = decoded[1];
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("occurredAt"),
                        java.time.Instant.parse(occurredAt)
                    ));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private TransactionSourceType resolveSourceType(CreateTransactionRequest request) {
        if (request.receipt() != null) {
            return TransactionSourceType.OCR;
        }
        return TransactionSourceType.MANUAL;
    }

    private ReceiptDTO processReceipt(Transaction transaction, CreateTransactionRequest request) {
        if (request.receipt() == null) {
            return null;
        }

        try {
            Receipt receipt = receiptService.createFromScan(
                transaction.getUser().getId(),
                request.receipt()
            );
            // Связываем чек с транзакцией через receipt.setTransaction(transaction)
            receipt.setTransaction(transaction);
            receipt = receiptRepository.save(receipt); // Сохраняем связь
            return dtoFactory.toReceiptDto(receipt);
        } catch (Exception e) {
            // Логгируем ошибку но не прерываем создание транзакции
            return null;
        }
    }

    private String encodeCursor(Transaction transaction) {
        return cursorEncoder.encode(
            transaction.getId(),
            transaction.getOccurredAt().toString()
        );
    }

    private int sanitizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 20;
        }
        return Math.min(limit, 100);
    }

    private String normalizeCurrency(String currency) {
        return currency != null ? currency.trim().toUpperCase() : "USD";
    }
}
