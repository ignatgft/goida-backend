package ru.goidaai.test_backend.application.port.out;

import ru.goidaai.test_backend.domain.AssetPool;
import java.util.List;
import java.util.Optional;

/**
 * Порт репозитория для AssetPool
 */
public interface AssetPoolRepositoryPort {
    AssetPool save(AssetPool assetPool);
    Optional<AssetPool> findById(String id);
    List<AssetPool> findByUserId(String userId);
    void deleteById(String id);
    boolean existsById(String id);
}
