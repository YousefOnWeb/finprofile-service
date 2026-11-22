package com.financial.profile.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.financial.profile.model.RiskTolerance;

/**
 * Response DTO handed back to the API caller.
 *
 * <p>Analogy: this is the "receipt" you give the customer after processing
 * their request. It contains only the information the caller should see —
 * never internal database details.</p>
 */
public record ProfileResponse(
        String id,
        String fullName,
        String email,
        BigDecimal monthlyIncome,
        BigDecimal netWorth,
        RiskTolerance riskTolerance,
        Instant createdAt,
        Instant updatedAt
) {}
