package com.financial.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.financial.profile.model.FinancialProfile;

/**
 * Spring Data JPA repository for financial profile persistence.
 *
 * <p>Analogy: this is the filing system interface, but now Spring Data JPA
 * provides the implementation automatically. We only declare the contract
 * — Spring generates the SQL and manages the database connection.</p>
 *
 * <p>By extending {@link JpaRepository}, we inherit standard CRUD methods:
 * {@code save}, {@code findById}, {@code deleteById}, {@code findAll}, and
 * more — without writing a single line of implementation code.</p>
 */
@Repository
public interface FinancialProfileRepository extends JpaRepository<FinancialProfile, String> {

    /**
     * Check whether a profile with the given email already exists.
     *
     * <p>This is a derived query method: Spring Data JPA generates the
     * underlying SQL from the method name.</p>
     *
     * @param email the email to check
     * @return {@code true} if a profile with this email exists
     */
    boolean existsByEmail(String email);
}
