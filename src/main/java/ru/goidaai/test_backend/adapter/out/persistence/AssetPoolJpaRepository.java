package ru.goidaai.test_backend.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.goidaai.test_backend.application.port.out.AssetPoolRepositoryPort;
import ru.goidaai.test_backend.domain.AssetPool;

import java.util.List;
import java.util.Optional;

/**
 * Адаптер репозитория для AssetPool
 */
@Repository
@RequiredArgsConstructor
public class AssetPoolJpaRepository implements AssetPoolRepositoryPort {

    private final AssetPoolEntityRepository repository;

    @Override
    public AssetPool save(AssetPool assetPool) {
        AssetPoolEntity entity = AssetPoolEntity.fromDomain(assetPool);
        AssetPoolEntity saved = repository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<AssetPool> findById(String id) {
        return repository.findById(id).map(AssetPoolEntity::toDomain);
    }

    @Override
    public List<AssetPool> findByUserId(String userId) {
        return repository.findAllByUserId(userId).stream()
            .map(AssetPoolEntity::toDomain)
            .toList();
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return repository.existsById(id);
    }
}

/**
 * Внутренний интерфейс для Spring Data JPA
 */
interface AssetPoolEntityRepository extends org.springframework.data.jpa.repository.JpaRepository<AssetPoolEntity, String> {
    List<AssetPoolEntity> findAllByUserId(String userId);
}
