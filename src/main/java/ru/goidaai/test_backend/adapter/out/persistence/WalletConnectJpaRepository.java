package ru.goidaai.test_backend.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.goidaai.test_backend.application.port.out.WalletConnectRepositoryPort;
import ru.goidaai.test_backend.domain.WalletConnect;

import java.util.List;
import java.util.Optional;

/**
 * Адаптер репозитория для WalletConnect
 */
@Repository
public interface WalletConnectJpaRepository extends JpaRepository<WalletConnectEntity, String>, WalletConnectRepositoryPort {

    Optional<WalletConnectEntity> findByUserIdAndWalletAddress(String userId, String walletAddress);

    @Override
    default WalletConnect save(WalletConnect walletConnect) {
        WalletConnectEntity entity = WalletConnectEntity.fromDomain(walletConnect);
        WalletConnectEntity saved = super.save(entity);
        return saved.toDomain();
    }

    @Override
    default Optional<WalletConnect> findById(String id) {
        return super.findById(id).map(WalletConnectEntity::toDomain);
    }

    @Override
    default Optional<WalletConnect> findByUserIdAndWalletAddress(String userId, String walletAddress) {
        return findByUserIdAndWalletAddress(userId, walletAddress).map(WalletConnectEntity::toDomain);
    }

    @Override
    default List<WalletConnect> findByUserId(String userId) {
        return findAllByUserId(userId).stream()
            .map(WalletConnectEntity::toDomain)
            .toList();
    }

    @Override
    default void deleteById(String id) {
        super.deleteById(id);
    }

    List<WalletConnectEntity> findAllByUserId(String userId);
}
