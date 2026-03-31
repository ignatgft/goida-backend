package ru.goidaai.test_backend.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.goidaai.test_backend.domain.AssetPool;
import ru.goidaai.test_backend.domain.AssetPoolType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JPA сущность для AssetPool
 */
@Entity
@Table(name = "asset_pools", indexes = {
    @Index(name = "idx_asset_pools_user_id", columnList = "user_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetPoolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, name = "user_id")
    private String userId;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AssetPoolType type;

    @Column(nullable = false, length = 10, name = "base_currency")
    private String baseCurrency;

    @Column(nullable = false, precision = 19, scale = 8, name = "total_balance")
    private BigDecimal totalBalance;

    @Column(precision = 19, scale = 8, name = "total_value_in_base_currency")
    private BigDecimal totalValueInBaseCurrency;

    @Column(length = 500)
    private String note;

    @Column(nullable = false, name = "created_at")
    private Instant createdAt;

    @Column(nullable = false, name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (totalBalance == null) totalBalance = BigDecimal.ZERO;
        if (totalValueInBaseCurrency == null) totalValueInBaseCurrency = BigDecimal.ZERO;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public AssetPool toDomain() {
        return AssetPool.builder()
            .id(id)
            .userId(userId)
            .name(name)
            .type(type)
            .baseCurrency(baseCurrency)
            .totalBalance(totalBalance)
            .totalValueInBaseCurrency(totalValueInBaseCurrency)
            .note(note)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }

    public static AssetPoolEntity fromDomain(AssetPool pool) {
        return AssetPoolEntity.builder()
            .id(pool.getId())
            .userId(pool.getUserId())
            .name(pool.getName())
            .type(pool.getType())
            .baseCurrency(pool.getBaseCurrency())
            .totalBalance(pool.getTotalBalance())
            .totalValueInBaseCurrency(pool.getTotalValueInBaseCurrency())
            .note(pool.getNote())
            .createdAt(pool.getCreatedAt())
            .updatedAt(pool.getUpdatedAt())
            .build();
    }
}
