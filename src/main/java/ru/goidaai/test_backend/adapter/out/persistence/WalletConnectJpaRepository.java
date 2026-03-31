package ru.goidaai.test_backend.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.goidaai.test_backend.application.port.out.WalletConnectRepositoryPort;
import ru.goidaai.test_backend.domain.WalletConnect;

import java.util.List;
import java.util.Optional;

/**
 * Адаптер репозитория для WalletConnect
 */
@Repository
@RequiredArgsConstructor
public class WalletConnectJpaRepository implements WalletConnectRepositoryPort {

    private final WalletConnectEntityRepository repository;

    @Override
    public WalletConnect save(WalletConnect walletConnect) {
        WalletConnectEntity entity = WalletConnectEntity.fromDomain(walletConnect);
        WalletConnectEntity saved = repository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<WalletConnect> findById(String id) {
        return repository.findById(id).map(WalletConnectEntity::toDomain);
    }

    @Override
    public Optional<WalletConnect> findByUserIdAndWalletAddress(String userId, String walletAddress) {
        return repository.findByUserIdAndWalletAddress(userId, walletAddress)
            .map(WalletConnectEntity::toDomain);
    }

    @Override
    public List<WalletConnect> findByUserId(String userId) {
        return repository.findAllByUserId(userId).stream()
            .map(WalletConnectEntity::toDomain)
            .toList();
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}

/**
 * Внутренний интерфейс для Spring Data JPA
 */
interface WalletConnectEntityRepository extends org.springframework.data.jpa.repository.JpaRepository<WalletConnectEntity, String> {
    Optional<WalletConnectEntity> findByUserIdAndWalletAddress(String userId, String walletAddress);
    List<WalletConnectEntity> findAllByUserId(String userId);
}
