package com.financial.profile.repository;

import java.util.List;
import java.util.Optional;

import com.financial.profile.model.FinancialProfile;

/**
 * Repository contract for financial profile persistence.
 *
 * <p>Analogy: this is the "filing system interface." It declares what the
 * cabinet can do (save, find by ID, delete, check email existence) without
 * committing to how it's implemented internally.</p>
 */
public interface FinancialProfileRepository {

    /**
     * Save a profile. If a profile with the same ID already exists, it is
     * replaced.
     *
     * @param profile the profile to save
     * @return the saved profile
     */
    FinancialProfile save(FinancialProfile profile);

    /**
     * Find a profile by its ID.
     *
     * @param id the profile ID
     * @return an {@link Optional} containing the profile if found, or empty
     */
    Optional<FinancialProfile> findById(String id);

    /**
     * Check whether a profile with the given email already exists.
     *
     * @param email the email to check
     * @return {@code true} if a profile with this email exists
     */
    boolean existsByEmail(String email);

    /**
     * Delete a profile by its ID.
     *
     * @param id the profile ID
     * @return {@code true} if a profile was deleted, {@code false} if none
     *         existed with that ID
     */
    boolean deleteById(String id);

    /**
     * Retrieve all profiles.
     *
     * @return a list of all profiles
     */
    List<FinancialProfile> findAll();
}
