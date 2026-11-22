package com.financial.profile.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financial.profile.dto.ApiResponse;
import com.financial.profile.dto.CreateProfileRequest;
import com.financial.profile.dto.ProfileResponse;
import com.financial.profile.dto.UpdateProfileRequest;
import com.financial.profile.exception.ResourceNotFoundException;
import com.financial.profile.service.FinancialProfileService;

/**
 * REST controller for financial profiles.
 *
 * <p>Analogy: this is the front-desk receptionist. It receives HTTP requests,
 * delegates to the service layer (branch manager), and returns responses
 * wrapped in the standard {@link ApiResponse} envelope. It never touches the
 * repository directly.</p>
 *
 * <ul>
 *   <li>{@code POST   /api/v1/profiles}   — create a profile</li>
 *   <li>{@code GET    /api/v1/profiles}   — list all profiles</li>
 *   <li>{@code GET    /api/v1/profiles/{id}} — retrieve one profile</li>
 *   <li>{@code PUT    /api/v1/profiles/{id}} — update a profile</li>
 *   <li>{@code DELETE /api/v1/profiles/{id}} — delete a profile</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/profiles")
public class FinancialProfileController {

    private final FinancialProfileService service;

    /**
     * Constructs the controller with the given service.
     *
     * @param service the business-logic component
     */
    public FinancialProfileController(FinancialProfileService service) {
        this.service = service;
    }

    /**
     * Create a new profile.
     *
     * @param request the validated creation DTO
     * @return 201 Created with the new profile in the envelope
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> create(
            @Valid @RequestBody CreateProfileRequest request) {
        ProfileResponse created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Profile created successfully", created));
    }

    /**
     * List all profiles.
     *
     * @return 200 OK with all profiles in the envelope
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProfileResponse>>> listAll() {
        List<ProfileResponse> profiles = service.listAll();
        return ResponseEntity.ok(
                ApiResponse.ok("Profiles retrieved successfully", profiles)
        );
    }

    /**
     * Retrieve a single profile by ID.
     *
     * @param id the profile ID
     * @return 200 OK with the profile in the envelope
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProfileResponse>> getById(
            @PathVariable String id) {
        ProfileResponse profile = service.getById(id);
        return ResponseEntity.ok(
                ApiResponse.ok("Profile retrieved successfully", profile)
        );
    }

    /**
     * Update an existing profile by ID.
     *
     * @param id      the profile ID
     * @param request the validated update DTO
     * @return 200 OK with the updated profile in the envelope
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProfileResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateProfileRequest request) {
        ProfileResponse updated = service.update(id, request);
        return ResponseEntity.ok(
                ApiResponse.ok("Profile updated successfully", updated)
        );
    }

    /**
     * Delete a profile by ID.
     *
     * @param id the profile ID
     * @return 200 OK with a confirmation message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>ok("Profile deleted successfully")
        );
    }
}
