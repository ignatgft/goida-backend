package ru.goidaai.test_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import ru.goidaai.test_backend.model.enums.AuthProvider;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    @Column(name = "monthly_budget", nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyBudget;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 32)
    private AuthProvider authProvider;

    @Column(name = "google_subject", nullable = false, unique = true, length = 255)
    private String googleSubject;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "last_login_at", nullable = false)
    private Instant lastLoginAt;

    @Column(name = "language", nullable = false, length = 10)
    private String language;

    @Column(name = "theme", nullable = false, length = 20)
    private String theme;

    @PrePersist
    public void applyDefaults() {
        if (baseCurrency == null || baseCurrency.isBlank()) {
            baseCurrency = "USD";
        }
        if (monthlyBudget == null) {
            monthlyBudget = BigDecimal.ZERO;
        }
        if (authProvider == null) {
            authProvider = AuthProvider.GOOGLE;
        }
        if (lastLoginAt == null) {
            lastLoginAt = Instant.now();
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public BigDecimal getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(BigDecimal monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    public String getGoogleSubject() {
        return googleSubject;
    }

    public void setGoogleSubject(String googleSubject) {
        this.googleSubject = googleSubject;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getName() {
        return fullName;
    }

    public void setName(String name) {
        this.fullName = name;
    }
}
