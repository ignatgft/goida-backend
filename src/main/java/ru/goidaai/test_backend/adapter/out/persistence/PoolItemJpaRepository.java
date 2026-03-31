package ru.goidaai.test_backend.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.goidaai.test_backend.application.port.out.PoolItemRepositoryPort;
import ru.goidaai.test_backend.domain.PoolItem;

import java.util.List;
import java.util.Optional;

/**
 * Адаптер репозитория для PoolItem
 */
@Repository
@RequiredArgsConstructor
public class PoolItemJpaRepository implements PoolItemRepositoryPort {

    private final PoolItemEntityRepository repository;

    @Override
    public PoolItem save(PoolItem poolItem) {
        PoolItemEntity entity = PoolItemEntity.fromDomain(poolItem);
        PoolItemEntity saved = repository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<PoolItem> findById(String id) {
        return repository.findById(id).map(PoolItemEntity::toDomain);
    }

    @Override
    public List<PoolItem> findByPoolId(String poolId) {
        return repository.findAllByPoolId(poolId).stream()
            .map(PoolItemEntity::toDomain)
            .toList();
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteByPoolId(String poolId) {
        repository.deleteAllByPoolId(poolId);
    }
}

/**
 * Внутренний интерфейс для Spring Data JPA
 */
interface PoolItemEntityRepository extends org.springframework.data.jpa.repository.JpaRepository<PoolItemEntity, String> {
    List<PoolItemEntity> findAllByPoolId(String poolId);
    void deleteAllByPoolId(String poolId);
}
