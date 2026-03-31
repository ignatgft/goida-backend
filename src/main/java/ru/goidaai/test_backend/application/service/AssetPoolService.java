package ru.goidaai.test_backend.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.goidaai.test_backend.application.port.out.AssetPoolRepositoryPort;
import ru.goidaai.test_backend.application.port.out.PoolItemRepositoryPort;
import ru.goidaai.test_backend.domain.AssetPool;
import ru.goidaai.test_backend.domain.PoolItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * Use case для управления пулами активов
 */
@Service
@RequiredArgsConstructor
public class AssetPoolService {

    private final AssetPoolRepositoryPort assetPoolRepository;
    private final PoolItemRepositoryPort poolItemRepository;

    @Transactional
    public AssetPool createPool(String userId, String name, String type, String baseCurrency) {
        AssetPool pool = AssetPool.builder()
            .userId(userId)
            .name(name)
            .type(AssetPoolType.valueOf(type.toUpperCase()))
            .baseCurrency(baseCurrency)
            .totalBalance(BigDecimal.ZERO)
            .totalValueInBaseCurrency(BigDecimal.ZERO)
            .build();
        
        AssetPool savedPool = assetPoolRepository.save(pool);
        
        // Создаем первый элемент пула с базовой валютой
        PoolItem item = PoolItem.builder()
            .poolId(savedPool.getId())
            .currency(baseCurrency)
            .balance(BigDecimal.ZERO)
            .valueInBaseCurrency(BigDecimal.ZERO)
            .weight(100.0)
            .build();
        poolItemRepository.save(item);
        
        return savedPool;
    }

    @Transactional(readOnly = true)
    public AssetPool getPool(String poolId) {
        return assetPoolRepository.findById(poolId)
            .orElseThrow(() -> new RuntimeException("Pool not found"));
    }

    @Transactional(readOnly = true)
    public List<AssetPool> getUserPools(String userId) {
        return assetPoolRepository.findByUserId(userId);
    }

    @Transactional
    public PoolItem addCurrencyToPool(String poolId, String currency, BigDecimal balance) {
        AssetPool pool = getPool(poolId);
        
        PoolItem item = PoolItem.builder()
            .poolId(poolId)
            .currency(currency)
            .balance(balance)
            .weight(0.0)
            .build();
        
        recalculatePoolWeights(poolId);
        return poolItemRepository.save(item);
    }

    @Transactional
    public void updateCurrencyBalance(String poolId, String currency, BigDecimal newBalance) {
        List<PoolItem> items = poolItemRepository.findByPoolId(poolId);
        PoolItem item = items.stream()
            .filter(i -> i.getCurrency().equals(currency))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Currency not found in pool"));
        
        item.setBalance(newBalance);
        poolItemRepository.save(item);
        recalculatePoolWeights(poolId);
    }

    @Transactional
    public void recalculatePoolWeights(String poolId) {
        List<PoolItem> items = poolItemRepository.findByPoolId(poolId);
        BigDecimal totalValue = items.stream()
            .map(PoolItem::getValueInBaseCurrency)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        for (PoolItem item : items) {
            if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
                item.setWeight(item.getValueInBaseCurrency()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalValue, 2, BigDecimal.ROUND_HALF_UP)
                    .doubleValue());
            } else {
                item.setWeight(0.0);
            }
            poolItemRepository.save(item);
        }
    }

    @Transactional
    public void deletePool(String poolId) {
        poolItemRepository.deleteByPoolId(poolId);
        assetPoolRepository.deleteById(poolId);
    }
}
