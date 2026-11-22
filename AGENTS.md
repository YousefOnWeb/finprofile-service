# Spring Boot Best Practices

This file defines the engineering standards for this Spring Boot project.
For Spring Boot project rules, see:
- https://docs.spring.io/spring-boot/reference/using/structuring-your-code.html
- https://docs.spring.io/spring-boot/docs/3.1.8/reference/html/using.html

## Core Principles

Keep dependencies explicit, boundaries clear, configuration externalized, and behavior testable.
The highest-value habits are:

1. **Constructor injection** with `private final` fields.
2. **DTOs at API boundaries** — never expose JPA entities directly.
3. **Narrow transactions** — `@Transactional` only where atomicity is needed.
4. **Feature-oriented packaging** — package-by-feature over package-by-layer.

---

## AI Agent Operating Model & Workflow

When operating within this Spring Boot codebase, your role is not to "build the entire app" in a single monolithic prompt. You operate as a rigorous, component-focused backend engineer working inside a strict engineering system:

**Specification → Small Task → Implementation → Automated Proof → Review → Commit**

The repository carries the project's permanent memory, architectural constraints, and rules, allowing you to execute reliable vertical slices across sessions without context drift, accidental refactoring, or state loss.

### The Practical Loop: Specify → Plan → Implement → Verify → Review → Commit

You must work in short, independently verifiable cycles—typically one vertical feature slice or bounded refactoring task at a time.

- **Specify:** The target behavior is defined and approved in a concise Markdown specification (`docs/specs/`).
- **Plan:** Inspect the relevant code, verify bean boundaries, and propose a precise implementation plan.
- **Implement:** Write code strictly adhering to approved Spring IoC and layered architecture patterns.
- **Verify:** Run targeted unit tests, slice tests, integration tests with Testcontainers, and the full build verification pipeline.
- **Review:** Inspect uncommitted diffs against architecture decision records (ADRs) and AGENTS.md.
- **Commit:** Produce atomic, reviewable Git commits and update the progress log.

Long sessions degrade agent performance due to context bloat, stale assumptions, and duplicated logic. Conclude work cleanly after a coherent slice, save durable state into files and Git, and hand off structured artifacts to the next session.

### Bounded Prompts & Step-by-Step Execution

When executing work, prompt types are bounded to prevent scope creep:

- **Planning Prompt:** Read specs and architecture docs. Do not edit files. Output an explanation of current behavior, the exact files to modify, the proposed constructor-injected components, test strategy, and identified risks. Code implementation may only begin after plan approval.
- **Implementation Prompt:** Implement only the approved plan. Follow AGENTS.md rules. Add or update tests. Execute verification scripts. Update `docs/progress.md`.
- **Review Prompt:** Act as a strict senior code reviewer in a fresh context. Inspect uncommitted diffs against architecture docs for layer violations, missing tests, circular dependencies, field injection, or scope leakage.

### Explanation & Communication Style

Explain everything as it is being done, in full detail and in a clear way that any developer with a computer science background can understand:

- State what is being changed, why it is being changed, and which files are affected before making the change.
- Explain the purpose of each annotation, class, method, and configuration property introduced.
- Clarify how data flows across layer boundaries (Controller → Service → Repository) and how DTOs map between them.
- Describe the reasoning behind validation rules, exception mappings, and transaction boundaries.
- Avoid unexplained jargon; define or link to concepts when they are first introduced.
- Assume the reader understands CS fundamentals but may not be familiar with this specific codebase or Spring Boot conventions.

---

## Project Structure

Put the main application class in a root package such as `com.example.myapp`. Avoid the default package. Spring Boot uses that root package as the basis for component scanning and JPA entity discovery; an accidental broad scan can pull in classes from dependencies.

Prefer **package-by-feature** over package-by-layer:

```text
com.example.myapp
├── orders
│   ├── OrderController.java
│   ├── OrderService.java
│   ├── OrderRepository.java
│   ├── Order.java
│   └── OrderDto.java
├── customers
└── common
```

- Related changes stay together.
- Module boundaries are visible.
- For larger systems, consider Spring Modulith to enforce domain-oriented boundaries.
- Prefer feature-first packages—`orders`, `billing`, `customers`—with layering inside each feature, rather than top-level `controllers/`, `services/`, and `repositories/` packages that can become dumping grounds.
- For small-scale or prototype services, package-by-layer (`controllers`, `services`, `repositories`, `model`, `dto`, `exception`) is acceptable; migrate to package-by-feature as the domain grows.

---

## Agent-Ready Repository Structure

A standard Spring Boot repository optimized for agentic development maintains strict documentation and script boundaries:

```text
my-spring-boot-app/
├── AGENTS.md
├── README.md
├── pom.xml                        # (or build.gradle.kts)
├── docs/
│   ├── product/
│   │   ├── vision.md
│   │   └── user-flows.md
│   ├── architecture/
│   │   ├── overview.md
│   │   ├── module-boundaries.md
│   │   └── decisions/             # Architecture Decision Records (ADRs)
│   ├── specs/                     # Feature specifications
│   │   ├── 001-initial-shell.md
│   │   └── 002-order-processing.md
│   └── progress.md                # Inter-session handoff log
├── scripts/
│   ├── verify.sh                  # Runs compilation & tests
│   └── package-smoke-test.sh      # Launches JAR & hits /actuator/health
├── src/
│   ├── main/java/com/example/app/
│   │   ├── core/                  # Pure domain logic & use cases (Zero Spring dependencies)
│   │   ├── application/           # Application services & orchestration
│   │   ├── infrastructure/        # JPA repositories, external clients, messaging
│   │   └── web/                   # Controllers, DTO records, exception handlers
│   └── main/resources/
│       ├── application.yml
│       ├── application-test.yml
│       └── db/migration/          # Flyway / Liquibase scripts
└── src/test/java/                 # Unit, Slice, and Testcontainers integration tests
```

---

## Dependency Injection & Spring IoC

Spring IoC works best when your classes stay plain, explicit, and testable: declare what they need, let the container supply it, and avoid making Spring itself a hard dependency in application logic. Spring’s own guidance favors **constructor injection for mandatory dependencies** and setter injection only for genuinely optional ones.

### Core principles

- **Use constructor injection** for required collaborators, ideally with `final` fields. It makes dependencies explicit, prevents partially initialized objects, and lets you instantiate the class in a unit test without Spring.
- **Use setter injection sparingly** for optional dependencies that have sensible defaults; otherwise, required dependencies should be constructor arguments.
- **Avoid field injection** in production code. It prevents `final` fields, hides the class’s real dependency list, and makes ordinary unit testing harder.
- **Keep classes container-agnostic.** Do not inject `ApplicationContext` into ordinary services or call `getBean()` from application code; that reintroduces service-locator coupling.

With a single constructor, Spring can use it automatically; `@Autowired` is unnecessary.

### Required

```java
@Service
public class OrderService {

    private final PaymentService paymentService;
    private final Clock clock;

    OrderService(PaymentService paymentService, Clock clock) {
        this.paymentService = paymentService;
        this.clock = clock;
    }
}
```

The example above uses `java.time.Clock` so time-dependent logic stays testable without calling `LocalDateTime.now()` directly.

### Stereotypes

Use annotations for their intended roles rather than making everything a generic `@Component`:

| Annotation | Intended role |
|---|---|
| `@Component` | Generic bean (fallback only) |
| `@Service` | Business logic |
| `@Repository` | Data access |
| `@RestController` | HTTP layer / controllers |

### Service Design

- Keep services **stateless**. A singleton bean is shared across requests and threads, so mutable instance state can create concurrency bugs.
- Request-specific state should come from method arguments or request context, not instance fields.
- Program to interfaces or abstractions where it makes implementations swappable and simplifies test doubles.
- Limit constructor parameters. A long dependency list usually signals that a class has too many responsibilities and should be split (consider refactoring at roughly 4–6 dependencies).

### Design beans well

| Practice | Why it matters |
|---|---|
| Program to interfaces or abstractions | Makes implementations swappable and simplifies test doubles. |
| Keep singleton beans stateless | A singleton is shared across requests/threads, so mutable instance state can create concurrency bugs. |
| Limit constructor parameters | A long dependency list usually signals that a class has too many responsibilities and should be split. |
| Use `@Bean` for external/third-party types | You often cannot annotate classes you do not own, so configure them in a `@Configuration` class. |
| Use `@Component`/stereotypes for your own application classes | Keeps routine bean registration concise and discoverable. |
| Fail fast at startup | Let Spring report missing or ambiguous wiring during context startup rather than discovering failures on the first production request. |

### Resolve ambiguity deliberately

When more than one bean implements the same interface, do not rely on field or parameter names. Use explicit mechanisms:

- `@Primary` marks the default candidate.
- `@Qualifier("name")` selects a specific bean.
- Inject `List<T>` or `Map<String, T>` when all implementations are legitimate strategy/plugin candidates.
- Use custom qualifier annotations for safer, more readable selection in larger codebases.

```java
public OrderService(
        @Qualifier("stripePaymentService") PaymentService paymentService,
        Clock clock) {
    ...
}
```

### Avoid circular dependencies

A constructor cycle such as `A -> B -> A` fails at startup. Do not hide it with field injection or broadly enable circular-reference support; instead, extract shared behavior into a third bean, introduce an interface/event boundary, or use `ObjectProvider<T>` only when late resolution is genuinely appropriate.

### Handle lifecycle correctly

- Put initialization that depends on injected beans in `@PostConstruct`, not the constructor.
- Release resources in `@PreDestroy`.
- Inject expensive or prototype-scoped beans through `ObjectProvider<T>` when a singleton would otherwise hold a stale or prematurely created instance.
- Inject abstractions such as `Clock` rather than calling `LocalDateTime.now()` directly, making time-dependent logic testable.

### Practical checklist

- Required dependency? Constructor injection with `final`.
- Optional dependency? Setter injection with a default.
- Multiple implementations? `@Primary`, `@Qualifier`, or collection injection.
- Need a bean at runtime? `ObjectProvider<T>`.
- Circular dependency? Refactor the design.
- Need Spring in a unit test? Usually a sign that the class is coupled to the framework.
- More than roughly 4–6 constructor dependencies? Consider splitting the class.

The central rule is simple: **make dependencies visible, immutable where possible, and supplied by the container—not discovered or created by the class itself.**

---

## What AGENTS.md Enforces

The project's root AGENTS.md acts as an absolute contract. Rules are concrete, enforceable, and supersede general assumptions:

- **Strict IoC & Dependency Injection:** Use constructor injection with `final` fields exclusively. Field injection (`@Autowired` on fields) and setter injection are strictly banned. Never use `ApplicationContext` or `beanFactory.getBean()` inside business services.
- **Layer Isolation:** Controllers must not access databases, filesystems, or external network APIs directly. All persistence goes through Spring Data repositories in the infrastructure layer.
- **Database Evolution:** All relational schema modifications require a versioned migration script (Flyway/Liquibase under `src/main/resources/db/migration/`) accompanied by migration tests.
- **Domain Purity:** The core domain layer must be testable using plain JUnit 5 without loading a Spring `ApplicationContext`.
- **Dependency Discipline:** Do not introduce new Maven/Gradle dependencies without explicit justification showing why existing starters or libraries are insufficient.

---

## Configuration

Externalize configuration. Bind it to typed records/classes with `@ConfigurationProperties` rather than scattering `@Value` throughout the code.

### Required

```java
@ConfigurationProperties(prefix = "mail")
public record MailProperties(String host, int port, String from) {}
```

```yaml
mail:
  host: smtp.example.com
  port: 587
  from: no-reply@example.com
```

### Profiles and Secrets

- Use Spring profiles for environment differences.
- Never commit production secrets.
- Keep secrets in environment variables or a secrets manager — not in Git or `application.yml`.

### Configuration Style

- Spring Boot favors Java-based configuration.
- Use one primary `@SpringBootApplication` class.
- Split specialized configuration into separate `@Configuration` classes when needed.

### Bean Configuration

- Prefer Java-based configuration (`@Configuration` plus `@Bean`) for new code; it is clearer, type-safe, and easier to navigate than XML.
- Use component scanning for your own components, but reserve `@Bean` methods for third-party types, conditional beans, and complex construction.
- Keep configuration classes focused; avoid injecting many dependencies directly into `@Configuration` classes because they are processed early and can trigger unexpected early initialization.
- Use `@Lazy` deliberately for costly resources, not as a default way to mask wiring or design problems.

---

## Layered Architecture & REST APIs

Use a **Controller → Service → Repository** flow with clear object boundaries: DTOs at the HTTP edge, domain/application models in the service layer, and persistence entities only at the data-access boundary. Dependencies should flow downward only; controllers should never call repositories directly, and entities should not become your public API contract.

### Boundary flow

```text
Client
  ↓ JSON
Controller
  ↕ Request DTO / Response DTO
Service / Application layer
  ↕ Domain model or command/result object
Repository
  ↕ Entity / persistence model
Database
```

### Boundary responsibilities

| Boundary | Objects |
|---|---|
| Controller | Request and response DTOs |
| Service / Application | Domain objects, commands, or application-specific result types |
| Repository | Entities, projections, or domain objects |

Never cross a boundary with a raw persistence entity in an HTTP response. Entities can expose internal fields, lazy relationships, and schema details that should not define your API.

Keep controllers thin: they validate the *shape* of a request, delegate to one service method, and map the result into a response DTO.

### Required

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(request);
    }
}
```

### DTO rules

Use DTOs deliberately, not as an automatic extra layer.

- Create a **separate request and response DTO** for each meaningful use case; `CreateOrderRequest`, `UpdateOrderRequest`, and `OrderResponse` usually have different required fields and lifecycles.
- Keep DTOs **immutable and behavior-free** where practical; in Java, records are a good fit.
- Put **format validation** on request DTOs: required fields, lengths, formats, ranges.
- Put **business validation** in the service or domain model: “this customer may not exceed their credit limit,” “a canceled order cannot be refunded,” etc.
- Avoid exposing IDs, audit fields, internal flags, passwords, or persistence relationships unless they are intentionally part of the public contract.
- Version DTOs with the API rather than changing an existing external contract incompatibly.

Example:

```java
// HTTP boundary
record CreateOrderRequest(
    @NotNull UUID customerId,
    @NotEmpty List<OrderItemRequest> items
) {}

record OrderResponse(
    UUID id,
    String status,
    BigDecimal total
) {}

// Service boundary
record PlaceOrderCommand(
    CustomerId customerId,
    List<OrderItem> items
) {}

// Domain boundary
class Order {
    void place() {
        // invariant: order must have items, customer must be eligible, etc.
    }
}
```

### Where mapping belongs

There are two defensible conventions; choose one consistently.

| Approach | Mapping location | Best for |
|---|---|---|
| **Controller-edge mapping** | Controller or dedicated mapper called by controller | Keeping services independent of API formats; useful when multiple transports consume the same service |
| **Application-layer mapping** | Service/application layer | Simple CRUD APIs, where the service is the application boundary and DTOs are its input/output contract |

For most monolithic Spring-style services, map **request DTO to command/domain input** and **domain/entity result to response DTO** in the service or a dedicated mapper component. The key is that neither the entity nor the domain object should know about HTTP DTOs.

Avoid putting `toDto()` methods on entities: it couples persistence code to the API and makes it harder to evolve either side independently.

### Rules

- **Do not expose JPA entities directly** in API responses. That couples the database model to the public contract and can trigger lazy-loading or serialization problems.
- Use **Bean Validation** on request DTOs (`@Valid`).
- Centralize error responses with `@RestControllerAdvice` and `@ExceptionHandler`. Clients get a consistent error shape instead of stack traces or framework-specific errors.

### URL and naming

REST API design works best when it is **resource-oriented, predictable, stateless, documented, and secure**. The goal is not “REST purity” for its own sake—it is a consistent contract that clients can learn once and use reliably.

| Element | Recommended convention | Example |
|---|---|---|
| Collections | Plural noun | `/v1/orders` |
| Individual resource | Collection plus stable identifier | `/v1/orders/123` |
| Nested relationship | Parent-to-child path | `/v1/customers/42/orders` |
| Multi-word resources | Kebab-case | `/v1/order-items` |
| Query parameters | Consistent casing, commonly camelCase | `/v1/orders?sortBy=createdAt&limit=20` |

Use lowercase paths, hyphens rather than underscores, and consistent terminology throughout the API. Avoid exposing implementation details, database IDs where unnecessary, or technology-specific extensions in URLs.

### HTTP methods

Design around **business resources**, not database tables or UI screens: use nouns such as `/orders`, `/customers`, and `/invoices`; avoid verbs such as `/getOrders` or `/createUser`. Keep requests **stateless**: each request must carry the authentication, authorization, and context needed to process it; do not rely on server-side session state. Use standard HTTP methods according to their semantics.

| Method | Typical use | Key property |
|---|---|---|
| `GET` | Retrieve a resource or collection | Safe and idempotent |
| `POST` | Create a resource or submit a command | Not necessarily idempotent |
| `PUT` | Replace a resource at a known URI | Idempotent |
| `PATCH` | Apply a partial update | Usually not inherently idempotent |
| `DELETE` | Remove a resource | Idempotent in HTTP semantics |
| `HEAD` | Retrieve metadata without a body | Safe |
| `OPTIONS` | Discover allowed operations | Safe |

Do not tunnel operations through `GET` or `POST`—for example, `POST /users/delete?id=5` is usually a design smell. For actions that do not map cleanly to CRUD, use a narrowly scoped action endpoint such as `POST /orders/123/cancel`, and document its semantics explicitly.

### Collections and payloads

Collections should support the operations clients actually need:

- **Pagination:** use stable, explicit parameters such as `limit` and a cursor or page token; avoid returning unbounded result sets.
- **Filtering:** use query parameters such as `status=active` or `createdAfter=2026-01-01`.
- **Sorting:** support explicit fields and direction, such as `sortBy=createdAt&sortOrder=desc`.
- **Sparse fieldsets:** optionally allow `fields=id,name,total` to reduce payload size.
- **Bulk operations:** provide them when clients otherwise need many sequential requests, especially for imports, updates, or deletes.

Use JSON as the default representation, declare content types through headers, and validate every input. Return validation failures with `400 Bad Request` and enough field-level detail for the client to correct the request.

### Errors and status codes

A useful error response should tell the client **what happened, why, and what to do next**.

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed.",
    "details": [
      {
        "field": "email",
        "issue": "must be a valid email address"
      }
    ],
    "requestId": "req_01H..."
  }
}
```

Use status codes consistently:

- `200` — successful read or update
- `201` — resource created
- `202` — asynchronous operation accepted
- `204` — successful operation with no response body
- `400` — malformed or invalid client request
- `401` — missing or invalid authentication
- `403` — authenticated but not authorized
- `404` — resource not found
- `409` — conflict, such as duplicate creation or stale update
- `422` — semantically invalid request, where appropriate
- `429` — rate limit exceeded
- `500` — server-side failure

### Versioning and evolution

Treat the API as a product with a compatibility contract. Prefer backward-compatible additions—new optional fields, new endpoints, and new query parameters—over breaking changes. When breaking changes are unavoidable, version deliberately and publish a deprecation policy with a clear migration path.

Common approaches:

- **URI versioning:** `/v1/orders`; simple, visible, and easy to route.
- **Header versioning:** `API-Version: 2026-11-09`; keeps URLs cleaner but is less discoverable.
- **Date-based versions:** useful for SaaS APIs that evolve frequently.

Avoid changing the meaning of an existing field, removing required fields, or altering authentication behavior without a new version or explicit compatibility policy.

### Documentation and contract

Write the **OpenAPI specification first**, then implement against it. This makes the API contract explicit, enables generated documentation and client SDKs, and supports contract and integration testing.

Documentation should include:

- Every endpoint, method, parameter, and response schema
- Authentication and authorization requirements
- Request and response examples, including errors
- Rate limits, pagination rules, and idempotency behavior
- Version history, deprecations, and breaking-change policy

### Practical reference design

```http
GET /v1/orders?status=paid&limit=20&cursor=eyJvcmRlcklkIjoiMTIzIn0
Authorization: Bearer <token>
Accept: application/json
```

```http
POST /v1/orders
Content-Type: application/json
Idempotency-Key: 7f3c9d1e-...

{
  "customerId": "cus_84",
  "items": [
    { "productId": "prod_12", "quantity": 2 }
  ]
}
```

```http
HTTP/1.1 201 Created
Location: /v1/orders/ord_981
Content-Type: application/json
ETag: "v3"

{
  "id": "ord_981",
  "status": "pending",
  "total": 4990,
  "links": {
    "self": "/v1/orders/ord_981",
    "customer": "/v1/customers/cus_84"
  }
}
```

This design uses plural resources, explicit pagination, a stable identifier, content negotiation, an idempotency key for a non-idempotent creation request, and links to related resources.

### Rules worth enforcing

- Use one naming convention everywhere; inconsistency is more damaging than choosing a less-than-perfect convention.
- Avoid “chatty” APIs that force clients to make many small requests to assemble one screen or workflow.
- Use `ETag`, `Last-Modified`, `Cache-Control`, and conditional requests where caching is safe.
- Use idempotency keys for payment, order-creation, and other operations where a retried request must not create duplicates.
- Log request metadata, authentication identity, status, latency, and errors—but avoid logging secrets or sensitive payloads.

---

## Services and Transactions

Put business rules in services. Make transaction boundaries deliberate.

### Transaction Rule

Apply `@Transactional` at the smallest service method that needs atomicity — **not** automatically on every repository or controller method.

### Layer responsibilities

| Layer | Owns | Should not own |
|---|---|---|
| **Controller** | HTTP routing, request parsing, input-format validation, status codes, response shaping | Business rules, transactions, persistence, direct repository access |
| **Service / application** | Use cases, business rules, orchestration, transaction boundaries, authorization decisions | HTTP concepts, database implementation details, DTO-to-HTTP concerns |
| **Repository / persistence** | Queries, persistence, database mapping, storage-specific operations | Business rules, transaction policy, HTTP validation |
| **DTO** | Stable external contract for a specific API use case | Business behavior, persistence annotations, internal state |

### Service-layer discipline

- Make the service the **transaction boundary**: one use case, one transaction, one place to reason about consistency.
- Services may orchestrate repositories, domain objects, domain services, external clients, and events.
- Do not make services mere pass-through wrappers around repositories; if a method adds no rule, orchestration, security decision, or transaction boundary, consider whether the extra layer is justified.
- Avoid injecting `HttpServletRequest`, HTTP headers, or framework-specific web types into services; pass explicit values or an application-level context instead.
- Prefer constructor injection and immutable dependencies so dependencies and test seams are explicit.

### Proxy Gotcha

Avoid calling `@Transactional` methods from within the same class when proxy-based behavior matters. Spring’s transaction advice is applied through proxies, so internal self-invocation can bypass it.

---

## Data Access

Use Spring Data JPA repositories for routine CRUD:

- Derived query methods for simple lookups.
- Explicit `@Query` or Criteria/Specification queries for nontrivial cases.
- DTO projections when a screen needs only a few columns.
- **Paginate any endpoint that can return an unbounded collection.**

### Example

```java
interface OrderRepository extends JpaRepository<Order, Long> {
    Page<OrderSummary> findByCustomerId(Long customerId, Pageable pageable);
}
```

### Repository discipline

Repositories should expose intention-revealing operations rather than generic data access:

```java
interface OrderRepository {
    Optional<Order> findById(OrderId id);
    List<Order> findPendingOrdersFor(CustomerId customerId);
    void save(Order order);
}
```

- Keep queries, persistence mechanics, and database-specific projections in this layer.
- Do not place business decisions such as “can this user cancel this order?” in repositories.
- Return domain objects, entities, or narrowly defined projections—not arbitrary raw rows or untyped result arrays.

### Money and Dates

- **Do not use `float` or `double` for money.** Use `BigDecimal`, preferably constructed from a string and with explicit rounding rules.
- Use `java.time` types — `Instant`, `LocalDate`, `ZonedDateTime`, `Duration` — instead of mutable legacy `Date`/`Calendar` APIs.

---

## Validation & Exception Handling

### Validation principles

Use **Jakarta Bean Validation at the boundary** to reject malformed input, then centralize exception-to-HTTP mapping in a `@RestControllerAdvice`. Keep validation declarative, return a consistent machine-readable error format (preferably RFC 9457 `ProblemDetail`), and never leak internals to clients.

- Validate **all external input**: request bodies, query parameters, path variables, headers, and uploaded files. OWASP recommends parsing safely, validating resulting values, and rejecting invalid requests rather than processing partially valid data.
- Validate **DTOs, not entities**: expose a dedicated request DTO so clients cannot set internal fields such as `id`, `createdAt`, roles, or audit fields.
- Put **format/shape rules** in Bean Validation: `@NotBlank`, `@Size`, `@Email`, `@Pattern`, `@Min`, `@Max`, and `@Positive`.
- Put **business rules** in the service layer: “email is already registered,” “insufficient balance,” or “user cannot change this resource.” These are domain invariants, not merely input-format checks.
- Keep **database constraints** as a final integrity boundary; they protect against race conditions and direct data access, but should not replace API validation.

### Use the right annotations

| Situation | Use | Notes |
|---|---|---|
| Request-body DTO | `@Valid @RequestBody` | Triggers field validation before the controller method runs. |
| Nested DTO / collection element | `@Valid` on the field | Cascades validation into nested objects. |
| Path/query/header parameters | `@Validated` on the controller plus constraints on parameters | Spring raises `HandlerMethodValidationException` when method-level constraints fail. |
| Different rules for create vs. update | Validation groups with `@Validated(Create.class)` | Avoid duplicating DTOs solely for create/update rules. |
| Complex or reusable rule | Custom `ConstraintValidator` | Example: a strong-password validator or a valid country-code validator. |
| Cross-field rules | Custom class-level validator | Example: `startDate` must precede `endDate`. |

```java
public record CreateUserRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Email @Size(max = 254) String email,
    @NotNull @Min(18) @Max(120) Integer age,
    @Valid @NotNull AddressRequest address
) {}
```

```java
@PostMapping("/users")
public ResponseEntity<UserResponse> create(
        @Valid @RequestBody CreateUserRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(userService.create(request));
}
```

`@Valid` activates validation for the annotated object; constraints directly on controller parameters trigger method validation instead.

### Centralize exceptions

Do not scatter `try/catch` blocks through controllers. Use one `@RestControllerAdvice` to translate exceptions into stable HTTP responses; `@RestControllerAdvice` applies across controllers and avoids repetitive per-endpoint handling.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
        LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleBodyValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
            .getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fe -> Objects.requireNonNullElse(fe.getDefaultMessage(), "Invalid value"),
                (a, b) -> a,
                LinkedHashMap::new
            ));

        ProblemDetail problem = ProblemDetail
            .forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "One or more fields are invalid");
        problem.setTitle("Validation failed");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ProblemDetail handleParameterValidation(HandlerMethodValidationException ex) {
        ProblemDetail problem = ProblemDetail
            .forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "One or more request parameters are invalid");
        problem.setTitle("Validation failed");
        return problem;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail
            .forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource not found");
        return problem;
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    ProblemDetail handleBusinessRule(BusinessRuleViolationException ex) {
        ProblemDetail problem = ProblemDetail
            .forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Business rule violation");
        return problem;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);

        ProblemDetail problem = ProblemDetail
            .forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred");
        problem.setTitle("Internal server error");
        return problem;
    }
}
```

Spring explicitly recommends handling both `MethodArgumentNotValidException` and `HandlerMethodValidationException`, because the exception raised depends on whether validation applies to an individual object or to method parameters.

### Return consistent errors

Use RFC 9457 Problem Details for a standardized JSON error contract. Spring supports `ProblemDetail` and `ErrorResponse`, and can render errors as `application/problem+json`.

```json
{
  "type": "https://api.example.com/problems/validation-error",
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more fields are invalid",
  "instance": "/api/users",
  "errors": {
    "email": "must be a well-formed email address",
    "age": "must be greater than or equal to 18"
  }
}
```

A good error contract includes:

- A stable, machine-readable error code or problem type.
- A human-readable, actionable message.
- Field-specific validation details.
- The HTTP status and request path.
- A correlation/request ID for support and tracing.

Spring Boot can enable built-in RFC 9457 handling with `spring.mvc.problemdetails.enabled=true`; for custom validation payloads, extend `ResponseEntityExceptionHandler` or add explicit `@ExceptionHandler` methods.

### Map exceptions deliberately

| Failure | HTTP status | Typical exception / response |
|---|---:|---|
| JSON syntax, missing required field, invalid format | 400 | `MethodArgumentNotValidException`, `HttpMessageNotReadableException` |
| Invalid path/query/header parameter | 400 | `HandlerMethodValidationException` |
| Resource does not exist | 404 | Custom `ResourceNotFoundException` |
| Conflict with existing state | 409 | Custom `DuplicateResourceException` or `BusinessRuleViolationException` |
| Authenticated user lacks permission | 403 | Spring Security access-denied handling |
| Unauthenticated request | 401 | Spring Security authentication entry point |
| Unexpected server failure | 500 | Generic `Exception` handler |

Use specific exception types rather than relying on `Exception` everywhere; reserve the generic handler for truly unexpected failures.

### Security and operations for API responses

- **Do not expose stack traces, SQL, driver errors, file paths, configuration, or secrets** in API responses. Log the real exception server-side, but return only a safe generic message to the client.
- **Log expected 4xx failures at `WARN` or `INFO`**, and unexpected 5xx failures at `ERROR` with the exception and correlation context.
- **Use a correlation ID** in both the response and logs so a client-reported failure can be traced to the exact server event.
- **Validate uploads explicitly**: enforce maximum size, allowed MIME types, extensions, filename rules, and malware scanning where appropriate; do not trust a client-supplied filename or content type alone.
- **Sanitize only where necessary**: validate structured input first; sanitize HTML/output contexts using an allowlist approach rather than trying to “clean” arbitrary input.
- **Test failure paths**: invalid payloads, boundary values, nested-object errors, duplicate resources, malformed IDs, authorization failures, and the generic 500 path.

### Practical rule of thumb

A clean layering is:

1. **Controller / DTO**: syntactic and structural validation with Bean Validation.
2. **Service**: business rules, authorization-aware checks, and domain exceptions.
3. **Global handler**: maps exceptions to a consistent `ProblemDetail` response.
4. **Database**: final integrity constraints and transactional safety.

This keeps controllers thin, makes client-facing errors predictable, and prevents invalid or malicious input from reaching business logic.

---

## Logging and Errors

Use SLF4J with parameterized messages rather than string concatenation:

```java
log.info("Processing order {} for customer {}", orderId, customerId);
```

### Rules

- Preserve the original exception as the cause when translating failures.
- Never swallow exceptions silently.
- Log enough context to diagnose failures.
- Never log passwords, tokens, card numbers, or other secrets.

### Toggleable Local Logging & Telemetry

Include local detailed logging and telemetry during development in a **toggleable** way, so it can be enabled for debugging and disabled in production or shared environments:

- Provide a logging level toggle (e.g., `logging.level.*` in `application.yml` / profiles) so verbose local logs can be switched on without code changes.
- Never log PII, secrets, tokens, passwords, or card numbers even in debug telemetry.
- Exclude any local telemetry or debug log files from version control via `.gitignore` (e.g., `*.log`, `logs/`, `debug.log`).
- Treat telemetry output as sensitive: do not commit it, and do not ship it to external services without explicit consent and sanitization.

---

## Testing

Test behavior through public APIs rather than implementation details. Use focused test slices where possible:

| Slice | Annotation | Use case |
|---|---|---|
| Web layer | `@WebMvcTest` | Controller / web-layer behavior |
| Data layer | `@DataJpaTest` | Repository queries |
| Full context | `@SpringBootTest` | Small number of end-to-end integration tests |
| Infrastructure | Testcontainers | Tests needing a real database, broker, or other infrastructure |

### Example

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @MockBean OrderService orderService;
    // request/response assertions
}
```

### Layered Architecture & Testing Strategy

To ensure lightning-fast verification without spinning up full stacks unnecessarily, map code and tests across strict boundaries:

| Layer | Responsibility | Primary Testing Strategy |
|---|---|---|
| Core / Domain | Business rules, value objects, domain entities, domain events. | Pure Unit Tests: Fast JUnit 5 tests, zero Spring context. |
| Application Services | Transaction boundaries (`@Transactional`), use case orchestration, mappers. | Unit Tests with Mocks: Mockito for repositories and clients. |
| Infrastructure | Spring Data JPA entities, JDBC clients, external web clients, caches. | Integration Tests: Testcontainers (PostgreSQL, Redis) + `@DataJpaTest`. |
| Web Layer | `@RestController`, request validation (`@Valid`), DTO records, `@ControllerAdvice`. | Slice Tests: `@WebMvcTest` with MockMvc validation. |
| Observability & Actuator | Health checks, metrics, startup probes. | Smoke Tests: Executable JAR launch check against `/actuator/health`. |

---

## Documentation & Writing

When writing documentation, READMEs, or onboarding guides, **assume the developer reading it doesn't have ANY dependencies or required tests at all.** Every step should be self-contained:

- List every prerequisite (Java version, Maven/Gradle, database, Docker, environment variables).
- Provide exact commands to install dependencies and run the project from a clean machine.
- Never assume a local database, running service, or pre-seeded data already exists.
- Document how to run tests (`mvn test` / `gradle test`) and what infrastructure they require.
- Include a "first-time setup" section that gets a new developer from zero to a passing test suite.
- When telling the reader to run any non-global command, specify the directory they should run it from (e.g., "from the project root").
- The README should be streamlined, straightforward, and guaranteed to work if simply well read and followed.
- Avoid phrases like "just run the app" or "dependencies are already installed."

### Documentation–Code Synchronization

Documentation across the entire repository must be continuously checked against the code—and the code against the documentation—to ensure sync, representation, and truthfulness:

- Treat documentation as source of truth that must match the running code; outdated docs are considered a defect.
- Verify that every documented endpoint, field, behavior, and configuration property exists in the code and behaves as described.
- Check that docs do not contradict each other across files (e.g., README, `AGENTS.md`, spec files, ADRs).
- When code changes, update all affected documentation in the same commit; do not let docs drift behind implementation.
- Resolve any contradiction between docs and code, or between one doc and another, before considering a task complete.

---

## Security and Operations

- Use Spring Security for authentication and authorization.
- Encode passwords with a modern password hasher such as BCrypt.
- Rely on parameterized queries / JPA to avoid SQL injection.
- Require **HTTPS for every endpoint**; do not rely on HTTP except perhaps for a redirect to HTTPS.
- Authenticate every request using a well-established mechanism such as **OAuth 2.0 access tokens**, API keys where appropriate, or mTLS for service-to-service traffic.
- Authorize at the **resource level**: verify that the caller may access the specific object, not merely that they are logged in.
- Never put secrets, tokens, or sensitive personal data in **URLs**, since URLs can appear in logs, proxies, and browser history.
- Apply least privilege, input validation, **rate limiting**, request-size limits, and audit logging.
- Keep secrets in environment variables or a secrets manager — not in Git or `application.yml`.
- In CI, manage dependency versions consistently and scan dependencies for known vulnerabilities; Spring Boot’s dependency management helps centralize versions for supported libraries.

---

## Git Best Practices

### Commits

- Make **atomic commits**: each commit should represent one logical change that can be understood and, if necessary, reverted independently.
- Use **imperative mood** and concise, imperative-style subject lines: “Add order validation,” not “Added order validation” or “Fixes bug.”
- Prefer **conventional commit prefixes** where they add clarity: `feat:`, `fix:`, `chore:`, `docs:`, `refactor:`, `test:`, `perf:`.
- Include a body when the “what” isn’t self-evident; explain *why* the change was made and any trade-offs considered.
- Do not commit secrets, credentials, tokens, API keys, or locally generated configuration (e.g., `application.yml` with production values). Use `.gitignore` to exclude build output, IDE files, and environment-specific files.
- Avoid committing generated artifacts (`target/`, `build/`, `*.class`, `.jar`, `.war`, IDE metadata like `.idea/`, `*.iml`).

### Branching

- Use short-lived **feature branches** created from the main branch (`main` or `master`).
- Name branches descriptively: `feature/place-order-validation`, `fix/order-total-rounding`, `chore/upgrade-spring-boot`.
- Keep branches focused on a single issue or feature; avoid long-lived branches that diverge significantly from main.
- Rebase or merge main into feature branches regularly to reduce integration pain; prefer rebase for a clean linear history on feature branches, merge for integration into main.
- Never force-push to shared branches (`main`, `release`, hotfix branches). Force-push only on your own unshared feature branch if absolutely necessary.

### Pull Requests

- Keep pull requests **small and focused**; large PRs receive less thorough review and are more likely to miss defects.
- Include a clear description: what changed, why, how to test, and any user-facing behavior changes or breaking API changes.
- Link the PR to its tracking issue or ticket.
- Require at least one **peer review** before merging; authors should not self-approve their own PRs.
- Address review comments or explicitly disagree with rationale; do not leave unresolved conversations.
- Ensure all CI checks pass before merging (build, tests, static analysis, security scans).
- Squash or rebase feature-branch commits into a clean, logical sequence before merging into main where the team values a linear history.

---

## GitHub CI/CD Best Practices

### Pipeline Design

- Run CI on every **push** and **pull request** to the repository; block merges until required checks pass.
- Keep the pipeline **fast and reliable**: run quick checks (compile, unit tests, lint) first so failures surface early, then slower checks (integration tests, container builds).
- Cache dependency resolution (Maven/Gradle wrapper and local repository) between runs to reduce build time.
- Use a **matrix strategy** only when genuinely needed (e.g., multiple Java versions); avoid unnecessary parallelism that increases flakiness and cost.
- Fail fast: configure the pipeline to stop on the first critical failure rather than running every stage regardless.

### Testing in CI

- Run unit tests (`mvn test` / `gradle test`) on every PR; they should be fast and not require external infrastructure.
- Run Testcontainers-based integration tests in CI where they need a real database or broker; use lightweight, pinned container images.
- Keep `@SpringBootTest` end-to-end tests to a small, critical subset; prefer `@WebMvcTest` and `@DataJpaTest` slices for speed.
- Do not skip tests to “make the build green”; fix flaky tests or isolate them rather than disabling them.

### Code Quality & Security

- Run static analysis (Checkstyle, SpotBugs, or equivalent) as a CI gate.
- Run dependency vulnerability scanning (GitHub Dependabot, Snyk, OWASP Dependency-Check, or equivalent) and fail on critical/high vulnerabilities.
- Enforce a minimum code-coverage threshold only where it adds value; prioritize testing behavior and risk areas over arbitrary line coverage.
- Build and scan container images for known vulnerabilities before publishing to a registry.

### Deployment & Promotion

- Use environment-specific GitHub secrets for production credentials; never embed them in workflow files or commit them to the repository.
- Promote artifacts through environments (e.g., `dev` → `staging` → `production`) with manual approval gates for production.
- Tag container images and releases with the Git commit SHA, not just `latest`, to guarantee traceability and reproducibility.
- Keep build artifacts and container images immutable after promotion; do not overwrite a tagged release.
- Document rollback procedures for failed deployments.

---

## Automated Proof & Verification Pipeline

Never claim a task is complete merely because the code compiles. You must execute a deterministic verification pipeline:

- **Compile & Lint:** `./mvnw clean compile spotless:check`
- **Unit Tests:** `./mvnw test`
- **Integration Tests (Testcontainers):** `./mvnw verify`
- **Package Build:** `./mvnw package`
- **Launch & Smoke Test:**
  ```bash
  java -jar target/*.jar --spring.profiles.active=test &
  # Wait for readiness and verify Actuator health endpoint
  curl -f http://localhost:8080/actuator/health
  ```

---

## Practical Default

For most new Spring Boot services, start with:

- Java 17+ or a current LTS release
- Maven or Gradle with Spring Boot starters
- Feature-based packages
- Constructor injection and immutable DTO records
- `@ConfigurationProperties` plus profiles
- DTOs, validation, and global exception handling
- Service-level transactions
- Pagination, projections, and Testcontainers-based integration tests

These defaults align with Spring Boot’s own recommendations and avoid the most common sources of coupling, test difficulty, and production surprises.

---

## Practical Milestone: Financial User Profile CRUD Service

A milestone for applying Spring IoC, layered architecture, DTO boundaries, Bean Validation, and global exception handling in a concrete, bounded project: a mock financial user profile service with CRUD operations and robust input validation.

### Scope

Cover exactly: Spring IoC & Dependency Injection, layered architecture (Controller → Service → Repository), DTO mapping, Jakarta Bean Validation at the boundary, Spring Web MVC / REST, and `@ControllerAdvice` global exception handling. Deliberately exclude: security filters, database migrations, distributed messaging, and external infrastructure (use an in-memory repository for this milestone).

### Recommended Project Layout

Group by technical layer for this scale:

```text
com.financial.profile
├── controller      # @RestController endpoints, request mappings
├── service         # Business logic layer
├── repository      # Data access layer (@Repository)
├── model           # Domain entities
├── dto             # Request/Response Data Transfer Objects
└── exception       # Global exception handling (@ControllerAdvice)
```

### Phase 1: Domain Models & DTOs

Define a `FinancialProfile` entity and separate Request/Response DTOs. Controllers never touch entities directly; they deal exclusively with DTOs.

Annotate the Request DTO (`CreateProfileRequest`) using `jakarta.validation.constraints`:

- `@NotBlank` for names and IDs
- `@Email` for user contact details
- `@PositiveOrZero` or `@DecimalMin("0.0")` for monthly income / net worth
- `@NotNull` for enums such as `RiskTolerance` (`CONSERVATIVE`, `MODERATE`, `AGGRESSIVE`)

Isolate validation messages by passing custom message strings where appropriate (e.g., `@NotBlank(message = "Full name cannot be blank")`).

### Phase 2: Repository Layer (`@Repository`)

Create a `FinancialProfileRepository` interface with an in-memory implementation backed by a thread-safe structure such as `ConcurrentHashMap<String, FinancialProfile>` to simulate persistence without an external database. Annotate it with `@Repository` so Spring's component scanner registers it as a bean eligible for DI.

### Phase 3: Service Layer (`@Service`)

Implement `FinancialProfileService` containing core business rules (e.g., checking if a profile email already exists before saving, mapping DTOs to entities and vice versa). Inject the repository exclusively via constructor injection. Keep the service stateless; state belongs in the repository layer or incoming request payloads.

### Phase 4: REST Controller & Standard JSON Envelope (`@RestController`)

Build `FinancialProfileController` mapped to `/api/v1/profiles`. Implement the four core CRUD endpoints (`POST`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`). Trigger validation during creation and updates using `@Valid` on the request body parameter.

Wrap all returns inside a generic `ApiResponse<T>` envelope to guarantee a consistent frontend contract:

```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": { ... }
}
```

### Phase 5: Global Exception Handling (`@ControllerAdvice`)

Create a `@RestControllerAdvice` class (`GlobalExceptionHandler`) to intercept errors globally across all controllers:

- Handle `MethodArgumentNotValidException`: extract all field error messages from Jakarta Validation and return them inside the `ApiResponse` envelope with HTTP 400 Bad Request.
- Handle custom domain exceptions (e.g., `ResourceNotFoundException`) and map them to HTTP 404 cleanly.

### Engineering Tips & Pitfalls to Avoid

- **Constructor injection over field injection:** Avoid `@Autowired` on private fields. Constructor injection ensures immutability, makes unit testing trivial without reflection, and fails fast at startup if a dependency is missing.
- **Stateless services:** Ensure `@Service` and `@Controller` beans remain strictly stateless.
- **DTOs at API boundaries:** Never expose JPA entities directly in API responses.
- **One request DTO per use case:** Separate create and update request DTOs as their required fields and lifecycles differ.
