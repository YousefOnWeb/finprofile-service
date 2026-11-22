# Progress Log

## 2026-11-22 — Initial project structure committed
- Commit `8272c23 feat: initial project structure with profile CRUD`
- Built FinancialProfile CRUD service across controller, service, repository, model, dto, exception layers.
- Standard `ApiResponse<T>` envelope and `GlobalExceptionHandler` in place.
- In-memory `ConcurrentHashMap` repository (no external DB).
- `mvn clean compile` and `mvn test` pass (no tests yet).

## 2026-11-22 — Complete: Add unit and slice tests (Spec 002)
- Spec: docs/specs/002-add-tests.md
- Implemented: FinancialProfileServiceTest (10 Mockito unit tests) and FinancialProfileControllerTest (7 @WebMvcTest slice tests).
- Added spring-boot-starter-test (test scope) to pom.xml.
- mvn test: 17 tests run, 0 failures, BUILD SUCCESS.

## 2026-11-22 — Complete: Spring Data JPA repository with H2 (Spec 003)
- Spec: docs/specs/003-spring-data-jpa-repository.md
- Converted FinancialProfile to JPA entity (@Entity, @Id, @GeneratedValue, @PrePersist, @PreUpdate).
- Converted FinancialProfileRepository to extend JpaRepository with derived existsByEmail query.
- Removed InMemoryFinancialProfileRepository (replaced by Spring Data JPA).
- Added application.yml with H2 embedded datasource and create-drop DDL.
- Added FinancialProfileRepositoryTest (8 @DataJpaTest tests against embedded H2).
- Updated service for new 5-arg entity constructor and JPA-generated IDs.
- mvn verify: 25 tests run (7 controller + 8 repository + 10 service), 0 failures, BUILD SUCCESS.

## 2026-11-22 — Complete: Add README.md (documentation)
- Created streamlined README.md with prerequisites, exact commands (from project root), API endpoints, project structure, concepts demonstrated, and deliberately excluded items.
- README verified against code: endpoints, test count (25), and package structure all match.
- Original milestone scope complete: Spring IoC, layered architecture, DTO boundaries, Bean Validation, Spring Web MVC/REST, @ControllerAdvice, JPA persistence, and testing.


