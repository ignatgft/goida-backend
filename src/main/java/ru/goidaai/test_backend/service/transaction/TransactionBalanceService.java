package ru.goidaai.test_backend.service.transaction;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.goidaai.test_backend.model.Asset;
import ru.goidaai.test_backend.model.enums.TransactionKind;
import ru.goidaai.test_backend.service.AssetsService;

/**
 * Сервис для управления балансом активов при операциях с транзакциями
 */
@Service
public class TransactionBalanceService {

    private final AssetsService assetsService;

    public TransactionBalanceService(AssetsService assetsService) {
        this.assetsService = assetsService;
    }

    /**
     * Рассчитать изменение баланса на основе типа транзакции
     */
    public BigDecimal calculateBalanceDelta(TransactionKind kind, BigDecimal amount) {
        return switch (kind) {
            case EXPENSE -> amount.negate();
            case INCOME -> amount;
            case TRANSFER -> amount.negate();
        };
    }

    /**
     * Обновить баланс актива при создании транзакции
     */
    @Transactional
    public void updateBalanceForTransaction(Asset asset, TransactionKind kind, BigDecimal amount) {
        if (asset == null) {
            return;
        }

        BigDecimal balanceDelta = calculateBalanceDelta(kind, amount);
        assetsService.updateBalance(asset.getId(), balanceDelta);
    }

    /**
     * Восстановить баланс актива при удалении транзакции
     */
    @Transactional
    public void restoreBalanceForTransaction(Asset asset, TransactionKind kind, BigDecimal amount) {
        if (asset == null) {
            return;
        }

        // Инвертируем изменение
        BigDecimal balanceDelta = calculateBalanceDelta(kind, amount).negate();
        assetsService.updateBalance(asset.getId(), balanceDelta);
    }

    /**
     * Обновить баланс при изменении транзакции
     */
    @Transactional
    public void updateBalanceForTransactionChange(
        Asset oldAsset,
        Asset newAsset,
        TransactionKind kind,
        BigDecimal oldAmount,
        BigDecimal newAmount
    ) {
        // Восстанавливаем баланс старого актива
        if (oldAsset != null) {
            BigDecimal oldDelta = calculateBalanceDelta(kind, oldAmount);
            assetsService.updateBalance(oldAsset.getId(), oldDelta.negate());
        }

        // Обновляем баланс нового актива
        if (newAsset != null) {
            BigDecimal newDelta = calculateBalanceDelta(kind, newAmount);
            assetsService.updateBalance(newAsset.getId(), newDelta);
        }
    }
}
