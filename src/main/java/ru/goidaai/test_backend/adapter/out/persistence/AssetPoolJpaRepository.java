package ru.goidaai.test_backend.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.goidaai.test_backend.application.port.out.AssetPoolRepositoryPort;
import ru.goidaai.test_backend.domain.AssetPool;

import java.util.List;
import java.util.Optional;

/**
 * Адаптер репозитория для AssetPool
 */
@Repository
public class AssetPoolJpaRepository extends JpaRepository<AssetPoolEntity, String> 
    implements AssetPoolRepositoryPort {

    @Override
    public AssetPool save(AssetPool assetPool) {
        AssetPoolEntity entity = AssetPoolEntity.fromDomain(assetPool);
        AssetPoolEntity saved = super.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<AssetPool> findById(String id) {
        return super.findById(id).map(AssetPoolEntity::toDomain);
    }

    @Override
    public List<AssetPool> findByUserId(String userId) {
        return findAllByUserId(userId).stream()
            .map(AssetPoolEntity::toDomain)
            .toList();
    }

    @Override
    public void deleteById(String id) {
        super.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return super.existsById(id);
    }

    List<AssetPoolEntity> findAllByUserId(String userId);
}
