package com.financial.profile.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.financial.profile.dto.CreateProfileRequest;
import com.financial.profile.dto.ProfileResponse;
import com.financial.profile.dto.UpdateProfileRequest;
import com.financial.profile.exception.ResourceNotFoundException;
import com.financial.profile.model.FinancialProfile;
import com.financial.profile.repository.FinancialProfileRepository;

/**
 * Service layer containing business rules for financial profiles.
 *
 * <p>Analogy: this is the branch manager. It receives request DTOs (order
 * forms), enforces business rules, maps between DTOs and domain entities, and
 * coordinates with the repository (filing cabinet). It is stateless — it
 * holds no customer data between requests.</p>
 *
 * <p>Constructor injection is used: the repository is declared as a
 * constructor parameter, so Spring automatically supplies it. We avoid
 * {@code @Autowired} on fields because constructor injection makes the
 * dependency explicit and the class easier to test.</p>
 */
@Service
public class FinancialProfileService {

    private final FinancialProfileRepository repository;

    /**
     * Constructs the service with the given repository.
     *
     * @param repository the data-access component for profiles
     */
    public FinancialProfileService(FinancialProfileRepository repository) {
        this.repository = repository;
    }

    /**
     * Create a new financial profile from a creation request.
     *
     * @param request the validated creation DTO
     * @return the response DTO for the created profile
     * @throws IllegalArgumentException if a profile with the same email exists
     */
    public ProfileResponse create(CreateProfileRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "A profile with email '" + request.email() + "' already exists."
            );
        }

        FinancialProfile entity = new FinancialProfile(
                request.fullName(),
                request.email(),
                request.monthlyIncome(),
                request.netWorth(),
                request.riskTolerance()
        );

        FinancialProfile saved = repository.save(entity);
        return toResponse(saved);
    }

    /**
     * Retrieve a profile by its ID.
     *
     * @param id the profile ID
     * @return the response DTO for the profile
     * @throws ResourceNotFoundException if no profile exists with the given ID
     */
    public ProfileResponse getById(String id) {
        FinancialProfile entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile with id '" + id + "' not found."
                ));
        return toResponse(entity);
    }

    /**
     * Update an existing profile by its ID.
     *
     * @param id      the profile ID
     * @param request the validated update DTO
     * @return the response DTO for the updated profile
     * @throws ResourceNotFoundException if no profile exists with the given ID
     * @throws IllegalArgumentException  if the email is already taken by another profile
     */
    public ProfileResponse update(String id, UpdateProfileRequest request) {
        FinancialProfile existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile with id '" + id + "' not found."
                ));

        // Check email uniqueness only if the email is changing
        if (!existing.getEmail().equalsIgnoreCase(request.email())
                && repository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "A profile with email '" + request.email() + "' already exists."
            );
        }

        existing.setFullName(request.fullName());
        existing.setEmail(request.email());
        existing.setMonthlyIncome(request.monthlyIncome());
        existing.setNetWorth(request.netWorth());
        existing.setRiskTolerance(request.riskTolerance());

        FinancialProfile updated = repository.save(existing);
        return toResponse(updated);
    }

    /**
     * Delete a profile by its ID.
     *
     * @param id the profile ID
     * @throws ResourceNotFoundException if no profile exists with the given ID
     */
    public void delete(String id) {
        boolean exists = repository.existsById(id);
        if (!exists) {
            throw new ResourceNotFoundException(
                    "Profile with id '" + id + "' not found."
            );
        }
        repository.deleteById(id);
    }

    /**
     * List all profiles.
     *
     * @return a list of response DTOs for all profiles
     */
    public List<ProfileResponse> listAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map a domain entity to its response DTO.
     *
     * <p>Analogy: this is the step of copying information from the internal
     * file cabinet record onto the customer receipt. The entity never leaves
     * the service layer.</p>
     *
     * @param entity the domain entity
     * @return the response DTO
     */
    private ProfileResponse toResponse(FinancialProfile entity) {
        return new ProfileResponse(
                entity.getId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getMonthlyIncome(),
                entity.getNetWorth(),
                entity.getRiskTolerance(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
