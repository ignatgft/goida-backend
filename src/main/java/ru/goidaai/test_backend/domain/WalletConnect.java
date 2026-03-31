package ru.goidaai.test_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Доменная модель - Подключенный WalletConnect кошелек
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletConnect {
    private String id;
    private String userId;
    private String walletAddress;
    private WalletType walletType;
    private Boolean isActive;
    private Instant connectedAt;
    private Instant lastSyncedAt;
}
