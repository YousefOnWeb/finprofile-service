package com.financial.profile.model;

/**
 * Describes a user's appetite for financial risk.
 *
 * <p>Think of this as the user's chosen "investment personality":
 * <ul>
 *   <li>{@code CONSERVATIVE} — prefers safety, low risk.</li>
 *   <li>{@code MODERATE} — balanced approach.</li>
 *   <li>{@code AGGRESSIVE} — willing to take higher risk for higher potential return.</li>
 * </ul></p>
 */
public enum RiskTolerance {
    CONSERVATIVE,
    MODERATE,
    AGGRESSIVE
}
