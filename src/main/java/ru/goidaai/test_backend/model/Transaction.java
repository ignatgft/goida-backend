package ru.goidaai.test_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import ru.goidaai.test_backend.model.enums.TransactionKind;
import ru.goidaai.test_backend.model.enums.TransactionSourceType;

@Entity
@Table(
    name = "transactions",
    indexes = {
        @Index(name = "idx_transactions_user_id", columnList = "user_id"),
        @Index(name = "idx_transactions_user_occurred_at", columnList = "user_id, occurred_at"),
        @Index(name = "idx_transactions_user_category", columnList = "user_id, category")
    }
)
public class Transaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_asset_id")
    private Asset sourceAsset;

    @Column(name = "source_asset_name", length = 120)
    private String sourceAssetName;

    @Column(length = 160)
    private String title;

    @Column(nullable = false, length = 64)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TransactionKind kind;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 32)
    private TransactionSourceType sourceType;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(length = 500)
    private String note;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @OneToOne(mappedBy = "transaction", fetch = FetchType.LAZY)
    private Receipt receipt;

    @PrePersist
    public void applyDefaults() {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Asset getSourceAsset() {
        return sourceAsset;
    }

    public void setSourceAsset(Asset sourceAsset) {
        this.sourceAsset = sourceAsset;
    }

    public String getSourceAssetName() {
        return sourceAssetName;
    }

    public void setSourceAssetName(String sourceAssetName) {
        this.sourceAssetName = sourceAssetName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public TransactionKind getKind() {
        return kind;
    }

    public void setKind(TransactionKind kind) {
        this.kind = kind;
    }

    public TransactionSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(TransactionSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }
}
