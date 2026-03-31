package ru.goidaai.test_backend.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.goidaai.test_backend.application.port.out.PoolItemRepositoryPort;
import ru.goidaai.test_backend.domain.PoolItem;

import java.util.List;
import java.util.Optional;

/**
 * Адаптер репозитория для PoolItem
 */
@Repository
public class PoolItemJpaRepository extends JpaRepository<PoolItemEntity, String>
    implements PoolItemRepositoryPort {

    @Override
    public PoolItem save(PoolItem poolItem) {
        PoolItemEntity entity = PoolItemEntity.fromDomain(poolItem);
        PoolItemEntity saved = super.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<PoolItem> findById(String id) {
        return super.findById(id).map(PoolItemEntity::toDomain);
    }

    @Override
    public List<PoolItem> findByPoolId(String poolId) {
        return findAllByPoolId(poolId).stream()
            .map(PoolItemEntity::toDomain)
            .toList();
    }

    @Override
    public void deleteById(String id) {
        super.deleteById(id);
    }

    @Override
    public void deleteByPoolId(String poolId) {
        deleteAllByPoolId(poolId);
    }

    List<PoolItemEntity> findAllByPoolId(String poolId);
    void deleteAllByPoolId(String poolId);
}
