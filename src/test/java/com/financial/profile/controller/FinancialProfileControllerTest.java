package com.financial.profile.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.financial.profile.dto.CreateProfileRequest;
import com.financial.profile.dto.ProfileResponse;
import com.financial.profile.dto.UpdateProfileRequest;
import com.financial.profile.model.RiskTolerance;
import com.financial.profile.service.FinancialProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice tests for {@link FinancialProfileController}.
 *
 * <p>Analogy: this tests the front-desk receptionist in isolation. We send
 * simulated HTTP requests and check the responses, using a pretend manager
 * ({@code @MockBean FinancialProfileService}) so the filing cabinet is never
 * involved.</p>
 *
 * <p>{@code @WebMvcTest} loads only the web layer — the controller, its
 * exception handler, and JSON mapping — not the full application context.
 * This keeps the test fast, per {@code agents.md}.</p>
 */
@WebMvcTest(FinancialProfileController.class)
class FinancialProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FinancialProfileService service;

    private ProfileResponse makeProfile() {
        return new ProfileResponse(
                "prof-123",
                "Jane Doe",
                "jane@example.com",
                new BigDecimal("5000.00"),
                new BigDecimal("20000.00"),
                RiskTolerance.MODERATE,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }

    @Test
    @DisplayName("POST /api/v1/profiles should create and return 201")
    void create_success_returns201() throws Exception {
        // Given: the pretend manager returns a created profile
        ProfileResponse created = makeProfile();
        when(service.create(any(CreateProfileRequest.class))).thenReturn(created);

        CreateProfileRequest request = new CreateProfileRequest(
                "Jane Doe",
                "jane@example.com",
                new BigDecimal("5000.00"),
                new BigDecimal("20000.00"),
                RiskTolerance.MODERATE
        );

        // When / Then: POST returns 201 with the standard envelope
        mockMvc.perform(post("/api/v1/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("prof-123"))
                .andExpect(jsonPath("$.data.email").value("jane@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/profiles with blank name should return 400")
    void create_invalidRequest_returns400() throws Exception {
        // Given: a request with a blank full name (violates @NotBlank)
        CreateProfileRequest request = new CreateProfileRequest(
                "   ", // blank name
                "jane@example.com",
                new BigDecimal("5000.00"),
                new BigDecimal("20000.00"),
                RiskTolerance.MODERATE
        );

        // When / Then: validation rejects it with 400
        mockMvc.perform(post("/api/v1/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/profiles should list all profiles")
    void listAll_returns200() throws Exception {
        // Given: the pretend manager returns two profiles
        when(service.listAll()).thenReturn(List.of(makeProfile()));

        // When / Then: GET returns 200 with the envelope
        mockMvc.perform(get("/api/v1/profiles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("prof-123"));
    }

    @Test
    @DisplayName("GET /api/v1/profiles/{id} should return the profile")
    void getById_found_returns200() throws Exception {
        // Given: the pretend manager finds the profile
        when(service.getById("prof-123")).thenReturn(makeProfile());

        // When / Then: GET by ID returns 200
        mockMvc.perform(get("/api/v1/profiles/prof-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("prof-123"))
                .andExpect(jsonPath("$.data.fullName").value("Jane Doe"));
    }

    @Test
    @DisplayName("GET /api/v1/profiles/{id} when missing should return 404")
    void getById_notFound_returns404() throws Exception {
        // Given: the pretend manager throws not-found
        when(service.getById(anyString()))
                .thenThrow(new com.financial.profile.exception.ResourceNotFoundException(
                        "Profile with id 'prof-missing' not found."
                ));

        // When / Then: GET by missing ID returns 404
        mockMvc.perform(get("/api/v1/profiles/prof-missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/profiles/{id} should update and return 200")
    void update_success_returns200() throws Exception {
        // Given: the pretend manager returns the updated profile
        ProfileResponse updated = makeProfile();
        when(service.update(anyString(), any(UpdateProfileRequest.class))).thenReturn(updated);

        UpdateProfileRequest request = new UpdateProfileRequest(
                "Jane Doe",
                "jane@example.com",
                new BigDecimal("5500.00"),
                new BigDecimal("25000.00"),
                RiskTolerance.AGGRESSIVE
        );

        // When / Then: PUT returns 200 with updated data
        mockMvc.perform(put("/api/v1/profiles/prof-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("prof-123"));
    }

    @Test
    @DisplayName("DELETE /api/v1/profiles/{id} should delete and return 200")
    void delete_success_returns200() throws Exception {
        // Given: the pretend manager deletes successfully
        doNothing().when(service).delete(anyString());

        // When / Then: DELETE returns 200
        mockMvc.perform(delete("/api/v1/profiles/prof-123"))
                .andExpect(status().isOk());
    }
}
