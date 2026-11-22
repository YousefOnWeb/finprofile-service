# FinProfile Service

A mock financial user profile service built with Spring Boot. This project demonstrates CRUD operations, robust input validation, layered architecture, DTO boundaries, and global exception handling — designed as a learning project for Java, backend development, and Spring Boot.

## What this project is

Think of this service as a **digital filing cabinet for financial profiles**. Customers (API callers) send requests to create, view, update, or delete their financial profile. A receptionist (Controller) takes the request, a branch manager (Service) enforces business rules, and a filing cabinet (Repository backed by H2) stores the records.

## Prerequisites

You need the following installed on your machine:

- **Java 17 or later** (OpenJDK or Oracle JDK)
- **Maven 3.6+** (or the Maven Wrapper `./mvnw`)

No database installation is required — the project uses an embedded H2 database that starts automatically.

## Quick start

All commands below are run **from the project root**.

### 1. Build the project

```bash
mvn clean compile
```

### 2. Run the application

```bash
mvn spring-boot:run
```

The service starts on `http://localhost:8080`.

### 3. Try the API

You can test the endpoints with `curl` or any HTTP client. Examples below are run from the project root.

**Create a profile:**

```bash
curl -X POST http://localhost:8080/api/v1/profiles \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Jane Doe",
    "email": "jane@example.com",
    "monthlyIncome": 5000.00,
    "netWorth": 20000.00,
    "riskTolerance": "MODERATE"
  }'
```

**Get all profiles:**

```bash
curl http://localhost:8080/api/v1/profiles
```

**Get a profile by ID** (replace `prof-123` with an actual ID from the list response):

```bash
curl http://localhost:8080/api/v1/profiles/prof-123
```

**Update a profile:**

```bash
curl -X PUT http://localhost:8080/api/v1/profiles/prof-123 \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Jane Smith",
    "email": "jane@example.com",
    "monthlyIncome": 5500.00,
    "netWorth": 25000.00,
    "riskTolerance": "AGGRESSIVE"
  }'
```

**Delete a profile:**

```bash
curl -X DELETE http://localhost:8080/api/v1/profiles/prof-123
```

## Run the tests

From the project root:

```bash
mvn test
```

This runs all unit tests, slice tests, and repository tests (25 tests total). No external database or infrastructure is needed — the embedded H2 database starts automatically for repository tests.

## API endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/profiles` | Create a new profile |
| `GET` | `/api/v1/profiles` | List all profiles |
| `GET` | `/api/v1/profiles/{id}` | Get a profile by ID |
| `PUT` | `/api/v1/profiles/{id}` | Update a profile by ID |
| `DELETE` | `/api/v1/profiles/{id}` | Delete a profile by ID |

All responses use a standard envelope:

```json
{
  "success": true,
  "message": "Profile created successfully",
  "data": { ... }
}
```

## Project structure

```text
src/main/java/com/financial/profile/
├── FinprofileServiceApplication.java  # Main entry point (@SpringBootApplication)
├── controller/
│   └── FinancialProfileController.java  # REST endpoints (@RestController)
├── service/
│   └── FinancialProfileService.java     # Business logic (@Service)
├── repository/
│   └── FinancialProfileRepository.java  # Spring Data JPA repository
├── model/
│   ├── FinancialProfile.java            # JPA entity
│   └── RiskTolerance.java               # Enum (CONSERVATIVE, MODERATE, AGGRESSIVE)
├── dto/
│   ├── ApiResponse.java                 # Standard response envelope
│   ├── CreateProfileRequest.java        # Create DTO (@Validated)
│   ├── UpdateProfileRequest.java        # Update DTO (@Validated)
│   └── ProfileResponse.java             # Response DTO
└── exception/
    ├── GlobalExceptionHandler.java      # Global exception handling (@ControllerAdvice)
    └── ResourceNotFoundException.java   # Custom not-found exception

src/test/java/com/financial/profile/
├── controller/FinancialProfileControllerTest.java  # @WebMvcTest slice tests
├── repository/FinancialProfileRepositoryTest.java  # @DataJpaTest repository tests
└── service/FinancialProfileServiceTest.java        # Mockito unit tests

src/main/resources/application.yml  # H2 datasource configuration
```

## Concepts demonstrated

| Concept | Where | What it does |
|---------|-------|--------------|
| Constructor injection | Service & Controller | Dependencies declared via constructor, supplied by Spring |
| Layered architecture | Controller → Service → Repository | Separation of HTTP, business rules, and data access |
| DTO boundaries | `dto/` package | Request/response objects; entities never exposed directly |
| Bean Validation | `CreateProfileRequest`, `UpdateProfileRequest` | `@NotBlank`, `@Email`, `@DecimalMin` on input |
| Standard JSON envelope | `ApiResponse<T>` | Consistent `success`/`message`/`data` shape |
| Global exception handling | `GlobalExceptionHandler` | `@ControllerAdvice` maps errors to 400/404/500 |
| JPA persistence | `FinancialProfileRepository` | `JpaRepository` + embedded H2 database |

## Deliberately excluded

To keep this learning project focused, the following are **not** included:

- **Spring Security** — no authentication or authorization filters
- **Database migrations** — uses Hibernate `create-drop` DDL instead of Flyway/Liquibase
- **Distributed messaging** — no Kafka, RabbitMQ, or similar
- **Testcontainers** — uses embedded H2 instead of external database containers
- **External infrastructure** — no Redis, external PostgreSQL, or message broker

These can be added in future iterations once the core concepts are comfortable.

## License

This is a learning project. No license specified.
