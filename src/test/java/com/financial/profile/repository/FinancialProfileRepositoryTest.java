package com.financial.profile.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.financial.profile.model.FinancialProfile;
import com.financial.profile.model.RiskTolerance;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for {@link FinancialProfileRepository}.
 *
 * <p>Analogy: this tests the actual filing cabinet — we save real profiles,
 * retrieve them, and verify they persist correctly. {@code @DataJpaTest}
 * configures an embedded H2 database that exists only for the duration of
 * these tests, then disappears.</p>
 *
 * <p>{@code @DataJpaTest} loads only the JPA infrastructure — entities,
 * repositories, and the embedded database — not the full application
 * context. This keeps tests fast and focused on the persistence layer.</p>
 */
@DataJpaTest
class FinancialProfileRepositoryTest {

    @Autowired
    private FinancialProfileRepository repository;

    private FinancialProfile saveProfile(String fullName, String email) {
        FinancialProfile profile = new FinancialProfile(
                fullName,
                email,
                new BigDecimal("5000.00"),
                new BigDecimal("20000.00"),
                RiskTolerance.MODERATE
        );
        return repository.save(profile);
    }

    @Test
    @DisplayName("save should persist a profile and assign an ID")
    void save_persistsProfileWithId() {
        // Given: a new profile with no ID
        FinancialProfile profile = new FinancialProfile(
                "John Smith",
                "john@example.com",
                new BigDecimal("6000.00"),
                new BigDecimal("30000.00"),
                RiskTolerance.CONSERVATIVE
        );

        // When: we save it
        FinancialProfile saved = repository.save(profile);

        // Then: the ID is auto-generated and the profile is stored
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFullName()).isEqualTo("John Smith");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findById should return the profile when it exists")
    void findById_existing_returnsProfile() {
        // Given: a saved profile
        FinancialProfile saved = saveProfile("Jane Doe", "jane@example.com");

        // When: we look it up by ID
        Optional<FinancialProfile> found = repository.findById(saved.getId());

        // Then: the profile is returned
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    @DisplayName("findById should return empty when the profile does not exist")
    void findById_missing_returnsEmpty() {
        // When: we look up a non-existent ID
        Optional<FinancialProfile> found = repository.findById("non-existent-id");

        // Then: the result is empty
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail should return true when the email is taken")
    void existsByEmail_taken_returnsTrue() {
        // Given: a saved profile with this email
        saveProfile("Jane Doe", "jane@example.com");

        // When / Then: the email exists
        assertThat(repository.existsByEmail("jane@example.com")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail should return false when the email is free")
    void existsByEmail_free_returnsFalse() {
        // When / Then: no profile with this email
        assertThat(repository.existsByEmail("nobody@example.com")).isFalse();
    }

    @Test
    @DisplayName("deleteById should remove the profile")
    void deleteById_removesProfile() {
        // Given: a saved profile
        FinancialProfile saved = saveProfile("Jane Doe", "jane@example.com");

        // When: we delete it
        repository.deleteById(saved.getId());

        // Then: it is no longer found
        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("findAll should return all persisted profiles")
    void findAll_returnsAllProfiles() {
        // Given: multiple saved profiles
        saveProfile("Jane Doe", "jane@example.com");
        saveProfile("Bob Smith", "bob@example.com");

        // When: we list all
        List<FinancialProfile> all = repository.findAll();

        // Then: both are returned
        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("save should update an existing profile when saving again")
    void save_updatesExistingProfile() {
        // Given: a saved profile
        FinancialProfile saved = saveProfile("Jane Doe", "jane@example.com");
        saved.setFullName("Jane Smith");

        // When: we save the modified profile
        FinancialProfile updated = repository.save(saved);

        // Then: the update is reflected on re-read
        Optional<FinancialProfile> found = repository.findById(updated.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Jane Smith");
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }
}
