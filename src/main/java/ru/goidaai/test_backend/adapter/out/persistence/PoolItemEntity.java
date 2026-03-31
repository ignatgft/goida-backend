package ru.goidaai.test_backend.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.goidaai.test_backend.domain.PoolItem;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JPA сущность для PoolItem
 */
@Entity
@Table(name = "pool_items", indexes = {
    @Index(name = "idx_pool_items_pool_id", columnList = "pool_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoolItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, name = "pool_id")
    private String poolId;

    @Column(nullable = false, length = 20)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal balance;

    @Column(precision = 19, scale = 8, name = "value_in_base_currency")
    private BigDecimal valueInBaseCurrency;

    private Double weight;

    @Column(nullable = false, name = "created_at")
    private Instant createdAt;

    @Column(nullable = false, name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (balance == null) balance = BigDecimal.ZERO;
        if (valueInBaseCurrency == null) valueInBaseCurrency = BigDecimal.ZERO;
        if (weight == null) weight = 0.0;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public PoolItem toDomain() {
        return PoolItem.builder()
            .id(id)
            .poolId(poolId)
            .currency(currency)
            .balance(balance)
            .valueInBaseCurrency(valueInBaseCurrency)
            .weight(weight)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }

    public static PoolItemEntity fromDomain(PoolItem item) {
        return PoolItemEntity.builder()
            .id(item.getId())
            .poolId(item.getPoolId())
            .currency(item.getCurrency())
            .balance(item.getBalance())
            .valueInBaseCurrency(item.getValueInBaseCurrency())
            .weight(item.getWeight())
            .createdAt(item.getCreatedAt())
            .updatedAt(item.getUpdatedAt())
            .build();
    }
}
