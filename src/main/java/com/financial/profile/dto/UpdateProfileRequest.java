package com.financial.profile.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import com.financial.profile.model.RiskTolerance;

/**
 * Request DTO for updating an existing financial profile.
 *
 * <p>Analogy: this is the "change details form." Unlike the creation form,
 * fields here are optional — you only send the fields you want to change.
 * Any field that is provided is still validated (for example, if you supply
 * an email, it must be a valid email address).</p>
 */
public record UpdateProfileRequest(
        @NotBlank(message = "Full name cannot be blank") String fullName,
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email must be a valid email address") String email,
        @NotNull(message = "Monthly income cannot be null")
        @DecimalMin(value = "0.0", message = "Monthly income must not be negative") BigDecimal monthlyIncome,
        @NotNull(message = "Net worth cannot be null")
        @DecimalMin(value = "0.0", message = "Net worth must not be negative") BigDecimal netWorth,
        @NotNull(message = "Risk tolerance cannot be null") RiskTolerance riskTolerance
) {}
