# Specification 003: Spring Data JPA Repository with H2

## Goal
Replace the in-memory `ConcurrentHashMap` repository with a Spring Data JPA repository backed by an embedded H2 database, and add `@DataJpaTest` repository tests.

## In scope
- Add H2 database dependency to `pom.xml` (runtime scope).
- Add `src/main/resources/application.yml` with H2 datasource config and Hibernate DDL auto for dev/test.
- Convert `FinancialProfile` to a JPA entity (`@Entity`, `@Id`, `@GeneratedValue`, etc.).
- Convert `FinancialProfileRepository` to extend `JpaRepository<FinancialProfile, String>`.
- Remove `InMemoryFinancialProfileRepository` (replaced by Spring Data JPA).
- Add `src/test/java/com/financial/profile/repository/FinancialProfileRepositoryTest.java` with `@DataJpaTest`.
- Update `docs/progress.md`.

## Out of scope
- Flyway/Liquibase migrations (noted as future work per `agents.md`).
- Testcontainers with external PostgreSQL (H2 is sufficient for this slice).
- `@SpringBootTest` full-context tests.
- Security or distributed messaging.

## Acceptance criteria
- `mvn verify` passes (compile + unit + `@DataJpaTest` integration tests).
- Repository tests exercise save, findById, existsByEmail, deleteById, findAll against embedded H2.
- Service and controller tests still pass against the JPA-backed repository.
- No field injection; constructor injection preserved.
