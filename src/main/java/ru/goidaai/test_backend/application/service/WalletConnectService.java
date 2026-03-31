package ru.goidaai.test_backend.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.goidaai.test_backend.application.port.out.WalletConnectRepositoryPort;
import ru.goidaai.test_backend.domain.WalletConnect;
import ru.goidaai.test_backend.domain.WalletType;

import java.time.Instant;
import java.util.List;

/**
 * Use case для управления подключенными WalletConnect кошельками
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletConnectService {

    private final WalletConnectRepositoryPort walletConnectRepository;

    @Transactional
    public WalletConnect connectWallet(String userId, String walletAddress, String walletType) {
        // Проверяем, не подключен ли уже этот кошелек
        walletConnectRepository.findByUserIdAndWalletAddress(userId, walletAddress)
            .ifPresent(wallet -> {
                wallet.setIsActive(true);
                wallet.setConnectedAt(Instant.now());
                walletConnectRepository.save(wallet);
            });
        
        // Создаем новое подключение
        WalletConnect walletConnect = WalletConnect.builder()
            .userId(userId)
            .walletAddress(walletAddress)
            .walletType(WalletType.valueOf(walletType.toUpperCase()))
            .isActive(true)
            .connectedAt(Instant.now())
            .lastSyncedAt(Instant.now())
            .build();
        
        return walletConnectRepository.save(walletConnect);
    }

    @Transactional(readOnly = true)
    public List<WalletConnect> getUserWallets(String userId) {
        return walletConnectRepository.findByUserId(userId);
    }

    @Transactional
    public void disconnectWallet(String walletId) {
        walletConnectRepository.deleteById(walletId);
    }

    @Transactional
    public void updateLastSynced(String walletId) {
        WalletConnect wallet = walletConnectRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));
        wallet.setLastSyncedAt(Instant.now());
        walletConnectRepository.save(wallet);
    }

    /**
     * Синхронизировать балансы криптовалют из подключенного кошелька
     * TODO: Реализовать интеграцию с WalletConnect API
     */
    @Transactional
    public void syncWalletBalances(String walletId) {
        WalletConnect wallet = walletConnectRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        log.info("Синхронизация балансов для кошелька: {}", wallet.getWalletAddress());
        
        // Здесь будет вызов WalletConnect API для получения балансов
        // и обновления соответствующих активов в пуле пользователя
        
        wallet.setLastSyncedAt(Instant.now());
        walletConnectRepository.save(wallet);
    }
}
