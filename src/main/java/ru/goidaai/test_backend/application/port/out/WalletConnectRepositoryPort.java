package ru.goidaai.test_backend.application.port.out;

import ru.goidaai.test_backend.domain.WalletConnect;
import java.util.List;
import java.util.Optional;

/**
 * Порт репозитория для WalletConnect
 */
public interface WalletConnectRepositoryPort {
    WalletConnect save(WalletConnect walletConnect);
    Optional<WalletConnect> findById(String id);
    Optional<WalletConnect> findByUserIdAndWalletAddress(String userId, String walletAddress);
    List<WalletConnect> findByUserId(String userId);
    void deleteById(String id);
}
