package com.financial.profile.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.financial.profile.dto.CreateProfileRequest;
import com.financial.profile.dto.ProfileResponse;
import com.financial.profile.dto.UpdateProfileRequest;
import com.financial.profile.exception.ResourceNotFoundException;
import com.financial.profile.model.FinancialProfile;
import com.financial.profile.model.RiskTolerance;
import com.financial.profile.repository.FinancialProfileRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FinancialProfileService}.
 *
 * <p>Analogy: this tests the branch manager in isolation, using a "stunt
 * double" (Mockito mock) for the filing cabinet ({@link FinancialProfileRepository}).
 * We program the double to return specific profiles or throw specific
 * conditions, then verify the manager makes the right decisions.</p>
 */
class FinancialProfileServiceTest {

    @Mock
    private FinancialProfileRepository repository;

    @InjectMocks
    private FinancialProfileService service;

    private FinancialProfile existingProfile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existingProfile = new FinancialProfile(
                "prof-123",
                "Jane Doe",
                "jane@example.com",
                new BigDecimal("5000.00"),
                new BigDecimal("20000.00"),
                RiskTolerance.MODERATE
        );
    }

    @Test
    @DisplayName("create should save a new profile and return its response DTO")
    void create_returnsCreatedProfile() {
        // Given: a creation request and an empty filing cabinet
        CreateProfileRequest request = new CreateProfileRequest(
                "John Smith",
                "john@example.com",
                new BigDecimal("6000.00"),
                new BigDecimal("30000.00"),
                RiskTolerance.CONSERVATIVE
        );
        when(repository.existsByEmail("john@example.com")).thenReturn(false);

        FinancialProfile saved = new FinancialProfile(
                "prof-456",
                "John Smith",
                "john@example.com",
                new BigDecimal("6000.00"),
                new BigDecimal("30000.00"),
                RiskTolerance.CONSERVATIVE
        );
        saved.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        saved.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        when(repository.save(any(FinancialProfile.class))).thenReturn(saved);

        // When: we ask the manager to create the profile
        ProfileResponse response = service.create(request);

        // Then: the profile was saved and mapped to a response DTO
        assertThat(response.id()).isEqualTo("prof-456");
        assertThat(response.fullName()).isEqualTo("John Smith");
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.riskTolerance()).isEqualTo(RiskTolerance.CONSERVATIVE);
        verify(repository).save(any(FinancialProfile.class));
    }

    @Test
    @DisplayName("create should throw IllegalArgumentException when email already exists")
    void create_duplicateEmail_throws() {
        // Given: the cabinet already has a profile with this email
        CreateProfileRequest request = new CreateProfileRequest(
                "John Smith",
                "jane@example.com",
                new BigDecimal("6000.00"),
                new BigDecimal("30000.00"),
                RiskTolerance.CONSERVATIVE
        );
        when(repository.existsByEmail("jane@example.com")).thenReturn(true);

        // When / Then: the manager rejects the duplicate
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("jane@example.com");
        verify(repository, never()).save(any(FinancialProfile.class));
    }

    @Test
    @DisplayName("getById should return the profile response when found")
    void getById_found_returnsResponse() {
        // Given: the cabinet contains the profile
        when(repository.findById("prof-123")).thenReturn(Optional.of(existingProfile));

        // When: we ask for the profile by ID
        ProfileResponse response = service.getById("prof-123");

        // Then: we get back the mapped response DTO
        assertThat(response.id()).isEqualTo("prof-123");
        assertThat(response.fullName()).isEqualTo("Jane Doe");
        assertThat(response.email()).isEqualTo("jane@example.com");
    }

    @Test
    @DisplayName("getById should throw ResourceNotFoundException when not found")
    void getById_notFound_throws() {
        // Given: the cabinet has no profile with this ID
        when(repository.findById("prof-missing")).thenReturn(Optional.empty());

        // When / Then: the manager throws a not-found exception
        assertThatThrownBy(() -> service.getById("prof-missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("prof-missing");
    }

    @Test
    @DisplayName("update should modify an existing profile and return its response")
    void update_existing_returnsUpdatedProfile() {
        // Given: the cabinet contains the profile
        when(repository.findById("prof-123")).thenReturn(Optional.of(existingProfile));
        when(repository.existsByEmail("jane@example.com")).thenReturn(true); // same email, OK

        UpdateProfileRequest request = new UpdateProfileRequest(
                "Jane Smith",
                "jane@example.com",
                new BigDecimal("5500.00"),
                new BigDecimal("25000.00"),
                RiskTolerance.AGGRESSIVE
        );
        FinancialProfile updated = new FinancialProfile(
                "prof-123",
                "Jane Smith",
                "jane@example.com",
                new BigDecimal("5500.00"),
                new BigDecimal("25000.00"),
                RiskTolerance.AGGRESSIVE
        );
        updated.setCreatedAt(existingProfile.getCreatedAt());
        updated.setUpdatedAt(Instant.parse("2026-02-01T00:00:00Z"));
        when(repository.save(any(FinancialProfile.class))).thenReturn(updated);

        // When: we update the profile
        ProfileResponse response = service.update("prof-123", request);

        // Then: the profile was updated and mapped
        assertThat(response.id()).isEqualTo("prof-123");
        assertThat(response.fullName()).isEqualTo("Jane Smith");
        assertThat(response.riskTolerance()).isEqualTo(RiskTolerance.AGGRESSIVE);
        verify(repository).save(any(FinancialProfile.class));
    }

    @Test
    @DisplayName("update should throw ResourceNotFoundException when profile does not exist")
    void update_notFound_throws() {
        // Given: the cabinet has no profile with this ID
        when(repository.findById("prof-missing")).thenReturn(Optional.empty());

        UpdateProfileRequest request = new UpdateProfileRequest(
                "Jane Smith",
                "jane@example.com",
                new BigDecimal("5500.00"),
                new BigDecimal("25000.00"),
                RiskTolerance.AGGRESSIVE
        );

        // When / Then: the manager throws not-found
        assertThatThrownBy(() -> service.update("prof-missing", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("update should throw IllegalArgumentException when new email is taken")
    void update_duplicateEmail_throws() {
        // Given: the cabinet contains the profile, but another profile already uses the new email
        when(repository.findById("prof-123")).thenReturn(Optional.of(existingProfile));
        when(repository.existsByEmail("someone-else@example.com")).thenReturn(true);

        UpdateProfileRequest request = new UpdateProfileRequest(
                "Jane Doe",
                "someone-else@example.com",
                new BigDecimal("5500.00"),
                new BigDecimal("25000.00"),
                RiskTolerance.AGGRESSIVE
        );

        // When / Then: the manager rejects the email change
        assertThatThrownBy(() -> service.update("prof-123", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("someone-else@example.com");
        verify(repository, never()).save(any(FinancialProfile.class));
    }

    @Test
    @DisplayName("delete should remove an existing profile")
    void delete_existing_removes() {
        // Given: the cabinet contains the profile
        when(repository.findById("prof-123")).thenReturn(Optional.of(existingProfile));
        when(repository.deleteById("prof-123")).thenReturn(true);

        // When: we delete the profile
        service.delete("prof-123");

        // Then: the deletion was attempted
        verify(repository).deleteById("prof-123");
    }

    @Test
    @DisplayName("delete should throw ResourceNotFoundException when profile does not exist")
    void delete_notFound_throws() {
        // Given: the cabinet has no profile with this ID
        when(repository.findById("prof-missing")).thenReturn(Optional.empty());

        // When / Then: the manager throws not-found
        assertThatThrownBy(() -> service.delete("prof-missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("listAll should return all profiles as response DTOs")
    void listAll_returnsAllProfiles() {
        // Given: the cabinet contains multiple profiles
        FinancialProfile second = new FinancialProfile(
                "prof-456",
                "Bob Smith",
                "bob@example.com",
                new BigDecimal("7000.00"),
                new BigDecimal("40000.00"),
                RiskTolerance.AGGRESSIVE
        );
        when(repository.findAll()).thenReturn(List.of(existingProfile, second));

        // When: we list all profiles
        List<ProfileResponse> responses = service.listAll();

        // Then: all profiles are mapped to response DTOs
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo("prof-123");
        assertThat(responses.get(1).id()).isEqualTo("prof-456");
    }
}
