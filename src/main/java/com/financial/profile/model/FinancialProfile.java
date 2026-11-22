package com.financial.profile.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * The domain entity representing a financial user profile.
 *
 * <p>This is a JPA entity mapped to a database table. Spring Data JPA
 * handles persistence automatically through the repository.</p>
 *
 * <p>Analogy: this is the blueprint for a row in the database table. Each
 * field maps to a column, and each instance represents one stored profile.</p>
 */
@Entity
@Table(name = "financial_profiles")
public class FinancialProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal netWorth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskTolerance riskTolerance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected FinancialProfile() {
        // JPA requires a no-arg constructor
    }

    public FinancialProfile(String fullName,
                            String email,
                            BigDecimal monthlyIncome,
                            BigDecimal netWorth,
                            RiskTolerance riskTolerance) {
        this.fullName = fullName;
        this.email = email;
        this.monthlyIncome = monthlyIncome;
        this.netWorth = netWorth;
        this.riskTolerance = riskTolerance;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public BigDecimal getNetWorth() {
        return netWorth;
    }

    public void setNetWorth(BigDecimal netWorth) {
        this.netWorth = netWorth;
    }

    public RiskTolerance getRiskTolerance() {
        return riskTolerance;
    }

    public void setRiskTolerance(RiskTolerance riskTolerance) {
        this.riskTolerance = riskTolerance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FinancialProfile that = (FinancialProfile) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FinancialProfile{"
                + "id='" + id + '\''
                + ", fullName='" + fullName + '\''
                + ", email='" + email + '\''
                + ", monthlyIncome=" + monthlyIncome
                + ", netWorth=" + netWorth
                + ", riskTolerance=" + riskTolerance
                + '}';
    }
}
