# Specification 002: Add Unit and Slice Tests

## Goal
Add automated tests covering the service layer (unit tests with Mockito) and the web layer (slice tests with `@WebMvcTest`) for the Financial User Profile CRUD service.

## In scope
- `FinancialProfileServiceTest`: pure JUnit 5 + Mockito tests for business rules (create, get, update, delete, duplicate email, not found).
- `FinancialProfileControllerTest`: `@WebMvcTest` slice tests for HTTP endpoints (status codes, JSON envelope, validation errors).
- `docs/progress.md`: inter-session handoff log updated.

## Out of scope
- Testcontainers integration tests (no external database in milestone scope).
- `@SpringBootTest` full-context tests (keep to a small critical subset per `agents.md`).
- Security, database migrations, or external infrastructure.

## Acceptance criteria
- `mvn test` passes with all tests green.
- Service tests exercise constructor-injected repository mock.
- Controller tests exercise `@Valid` validation and `GlobalExceptionHandler` mapping.
- No production code changes; tests only.
