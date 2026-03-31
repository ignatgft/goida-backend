package ru.goidaai.test_backend.application.port.out;

import ru.goidaai.test_backend.domain.PoolItem;
import java.util.List;
import java.util.Optional;

/**
 * Порт репозитория для PoolItem
 */
public interface PoolItemRepositoryPort {
    PoolItem save(PoolItem poolItem);
    Optional<PoolItem> findById(String id);
    List<PoolItem> findByPoolId(String poolId);
    void deleteById(String id);
    void deleteByPoolId(String poolId);
}
