package com.financial.profile.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.financial.profile.model.FinancialProfile;

/**
 * In-memory implementation of {@link FinancialProfileRepository}.
 *
 * <p>Analogy: this is the actual filing cabinet — a thread-safe map that
 * stores profiles in memory. {@link ConcurrentHashMap} allows multiple
 * threads to read and write safely without corrupting the data, just like a
 * well-organized cabinet where multiple clerks can work at once.</p>
 *
 * <p>{@code @Repository} tells Spring that this class is a data-access
 * component. Spring registers it as a bean so it can be injected into the
 * service layer.</p>
 */
@Repository
public class InMemoryFinancialProfileRepository implements FinancialProfileRepository {

    private final Map<String, FinancialProfile> profiles = new ConcurrentHashMap<>();

    @Override
    public FinancialProfile save(FinancialProfile profile) {
        String id = profile.getId();
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
            profile.setId(id);
        }
        profiles.put(id, profile);
        return profile;
    }

    @Override
    public Optional<FinancialProfile> findById(String id) {
        return Optional.ofNullable(profiles.get(id));
    }

    @Override
    public boolean existsByEmail(String email) {
        return profiles.values().stream()
                .anyMatch(profile -> email.equalsIgnoreCase(profile.getEmail()));
    }

    @Override
    public boolean deleteById(String id) {
        return profiles.remove(id) != null;
    }

    @Override
    public List<FinancialProfile> findAll() {
        return List.copyOf(profiles.values());
    }
}
