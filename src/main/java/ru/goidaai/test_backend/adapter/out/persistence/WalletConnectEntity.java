package ru.goidaai.test_backend.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.goidaai.test_backend.domain.WalletConnect;
import ru.goidaai.test_backend.domain.WalletType;

import java.time.Instant;

/**
 * JPA сущность для WalletConnect
 */
@Entity
@Table(name = "wallet_connect", indexes = {
    @Index(name = "idx_wallet_connect_user_id", columnList = "user_id"),
    @Index(name = "idx_wallet_connect_wallet_address", columnList = "wallet_address")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletConnectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, name = "user_id")
    private String userId;

    @Column(nullable = false, name = "wallet_address", length = 128)
    private String walletAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32, name = "wallet_type")
    private WalletType walletType;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(nullable = false, name = "connected_at")
    private Instant connectedAt;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @PrePersist
    public void prePersist() {
        connectedAt = Instant.now();
        if (isActive == null) isActive = true;
    }

    public WalletConnect toDomain() {
        return WalletConnect.builder()
            .id(id)
            .userId(userId)
            .walletAddress(walletAddress)
            .walletType(walletType)
            .isActive(isActive)
            .connectedAt(connectedAt)
            .lastSyncedAt(lastSyncedAt)
            .build();
    }

    public static WalletConnectEntity fromDomain(WalletConnect wallet) {
        return WalletConnectEntity.builder()
            .id(wallet.getId())
            .userId(wallet.getUserId())
            .walletAddress(wallet.getWalletAddress())
            .walletType(wallet.getWalletType())
            .isActive(wallet.getIsActive())
            .connectedAt(wallet.getConnectedAt())
            .lastSyncedAt(wallet.getLastSyncedAt())
            .build();
    }
}
