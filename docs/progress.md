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
- Next: consider adding @DataJpaTest repository tests or Testcontainers integration tests if external DB is introduced.
