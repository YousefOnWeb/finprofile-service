package com.financial.profile.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * The domain entity representing a financial user profile.
 *
 * <p>This is the internal "permanent record" stored by the repository. It is
 * intentionally separate from the DTOs so that the database model never leaks
 * into the public API.</p>
 */
public class FinancialProfile {

    private String id;
    private String fullName;
    private String email;
    private BigDecimal monthlyIncome;
    private BigDecimal netWorth;
    private RiskTolerance riskTolerance;
    private Instant createdAt;
    private Instant updatedAt;

    public FinancialProfile() {
    }

    public FinancialProfile(String id,
                            String fullName,
                            String email,
                            BigDecimal monthlyIncome,
                            BigDecimal netWorth,
                            RiskTolerance riskTolerance) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.monthlyIncome = monthlyIncome;
        this.netWorth = netWorth;
        this.riskTolerance = riskTolerance;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
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
