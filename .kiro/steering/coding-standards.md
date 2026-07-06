---
inclusion: fileMatch
fileMatchPattern: ['**/*.java', '**/pom.xml', '**/application*.yml']
---

# SecureClaims AI — Coding Standards

Java 17 · Spring Boot 3.x · Spring Data JPA · Spring Security · PostgreSQL · Maven

## Project Structure

Every service module follows this package layout under `com.secureclaims.{service-name}`:

```
config/        — Configuration classes (SecurityConfig, SwaggerConfig)
controller/    — REST controllers (thin: validation + delegation only)
dto/request/   — Inbound DTOs with Bean Validation
dto/response/  — Outbound DTOs (never expose entities)
entity/        — JPA entities
enums/         — Service-specific enumerations
exception/     — Custom exceptions + GlobalExceptionHandler
repository/    — Spring Data JPA repositories
service/       — Service interfaces
service/impl/  — Service implementations
security/      — JWT filter, token provider (identity-service only)
event/         — Event listeners and publishers
util/          — Stateless utility/helper classes
```

Module names use kebab-case (`claims-service`). Package names use lowercase dot notation (`com.secureclaims.claims`).

## Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Class | PascalCase | `ClaimService`, `JwtTokenProvider` |
| Interface | PascalCase (no I-prefix) | `NotificationSender` |
| Method | camelCase | `createClaim()`, `calculateScore()` |
| Variable | camelCase | `claimedAmount`, `riskLevel` |
| Constant | UPPER_SNAKE_CASE | `MAX_FILE_SIZE`, `JWT_EXPIRY_MS` |
| DB Column | snake_case | `claim_id`, `created_at` |
| REST Endpoint | kebab-case, plural nouns | `/claims`, `/fraud-scores` |
| DTO | Suffix `Request` / `Response` | `ClaimRequest`, `ClaimResponse` |
| Entity | Singular noun | `Claim`, `User`, `FraudAnalysis` |
| Repository | Suffix `Repository` | `ClaimRepository` |
| Service Impl | Suffix `Impl` | `ClaimServiceImpl` |
| Controller | Suffix `Controller` | `ClaimController` |

## Architecture Rules (Mandatory)

- Controllers MUST NOT contain business logic — only validate input and delegate to service.
- Services MUST NOT depend on HTTP-layer objects (`HttpServletRequest`, `ResponseEntity`).
- Entities MUST NOT appear in API responses — always map to response DTOs.
- Repositories MUST NOT be accessed directly from controllers — go through service layer.
- Cross-service contracts live in the `shared-events` module.
- Use `@ControllerAdvice` for centralized exception handling — never catch exceptions inline in controllers.

## Coding Rules

### General

- Use `final` for parameters and locals that are not reassigned.
- No magic numbers or strings — extract to constants or `application.yml`.
- Return `Optional<T>` instead of null from repository/service methods.
- Use `var` only when the type is obvious from the RHS.
- Max method length: 20 lines. Max class length: see Class Size Limits below.

### Dependency Injection

ALWAYS use constructor injection via Lombok `@RequiredArgsConstructor` with `private final` fields. NEVER use `@Autowired` field injection.

```java
@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {
    private final ClaimRepository claimRepository;
    private final ApplicationEventPublisher eventPublisher;
}
```

### DTOs

- Request DTOs: use `@Data`, include Bean Validation annotations (`@NotBlank`, `@NotNull`, `@Min`, `@Email`).
- Response DTOs: use `@Data` or `@Builder`.
- Add `@Schema(description = "...")` on all DTO fields for OpenAPI documentation.

### Entities

- Annotate with `@Entity` and `@Table(name = "...", schema = "...")`.
- Primary key: `UUID` with `@GeneratedValue(strategy = GenerationType.UUID)`.
- Include `createdAt` (`@CreationTimestamp`) and `updatedAt` (`@UpdateTimestamp`) audit fields.
- Use Lombok `@Getter` + `@Setter` + `@NoArgsConstructor` (NOT `@Data` — avoids JPA proxy issues with equals/hashCode).
- Enums stored as `@Enumerated(EnumType.STRING)`.

### Service Layer

- Define an interface; implement in a class suffixed `Impl` annotated with `@Service`.
- Use `@Transactional` on write methods, `@Transactional(readOnly = true)` on read methods.
- Throw custom exceptions (`ResourceNotFoundException`, `DuplicateResourceException`, `InvalidStatusTransitionException`) — never return null for "not found".

### Controller Layer

- Annotate with `@RestController` and `@RequestMapping("/api/v1/...")` or the appropriate service base path.
- Use `@Valid` on `@RequestBody` parameters.
- Return `ResponseEntity<T>` with correct HTTP status codes.
- Max 5 lines per method body (validate, delegate, respond).
- Every method MUST have `@Operation(summary = "...")` and `@ApiResponses`.

### Repository Layer

- Extend `JpaRepository<Entity, UUID>`.
- Simple lookups: derive from method names.
- Complex queries: use `@Query` with JPQL.
- Avoid native SQL unless required for performance.

## Exception Handling

Every service MUST have a `GlobalExceptionHandler` (`@RestControllerAdvice`) mapping:
- `MethodArgumentNotValidException` → 400
- `ResourceNotFoundException` → 404
- `AccessDeniedException` → 403
- Generic `Exception` → 500 (no stack trace in response)

Standard error response shape:

```json
{
  "timestamp": "2026-07-04T10:15:30Z",
  "status": 404,
  "error": "Not Found",
  "message": "Claim not found with id: '...'",
  "path": "/claims/abc-123"
}
```

Custom exception classes use descriptive constructors:

```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s: '%s'", resource, field, value));
    }
}
```

## Security

- JWT-based stateless auth on all endpoints except `/auth/register` and `/auth/login`.
- JWT signed with HMAC-SHA256; secret and expiry externalized via environment variables.
- Token in header: `Authorization: Bearer <token>`.
- Use `@PreAuthorize("hasRole('ADMIN')")` for admin endpoints — never check roles inside service logic.
- Roles in JWT: `ROLE_USER`, `ROLE_ADMIN`.
- Never log passwords, tokens, or secrets. Never return password hashes in responses.
- Use `@JsonIgnore` on sensitive entity fields.

## Configuration

### application.yml Pattern

```yaml
server:
  port: ${SERVER_PORT:8081}
spring:
  application:
    name: <service-name>
  datasource:
    url: jdbc:postgresql://localhost:5432/secureclaims?currentSchema=<schema>
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        default_schema: <schema>
    show-sql: false
app:
  jwt:
    secret: ${JWT_SECRET:default-dev-secret-change-in-production}
    expiry-ms: ${JWT_EXPIRY_MS:86400000}
management:
  endpoints:
    web:
      exposure:
        include: health
```

- Never hardcode secrets — always use `${ENV_VAR:default}`.
- Use `application-test.yml` with H2 for tests.

## Logging

- Use SLF4J via Lombok `@Slf4j` — never `System.out.println()`.
- Levels: `ERROR` (unrecoverable), `WARN` (recoverable), `INFO` (business events), `DEBUG` (dev details).
- Never log sensitive data.
- Include identifiers: `log.info("Claim created: claimId={}, userId={}", claimId, userId)`.
- Use MDC for correlation IDs.

## Testing

- Framework: JUnit 5 + Mockito.
- Test class: `{ClassName}Test`. Test method: `should_{behavior}_when_{condition}`.
- Unit tests: `@ExtendWith(MockitoExtension.class)`, mock all deps, test in isolation.
- Integration tests: `@SpringBootTest` + `@ActiveProfiles("test")` with H2 and `MockMvc`.
- Follow given/when/then structure in test bodies.
- Minimum 80% line coverage on service layer.

```java
@ExtendWith(MockitoExtension.class)
class ClaimServiceImplTest {
    @Mock private ClaimRepository claimRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @InjectMocks private ClaimServiceImpl claimService;

    @Test
    void should_createClaim_when_validRequest() {
        // given
        // when
        // then
    }
}
```

## Event-Driven Design

- Publish events inside `@Transactional` methods AFTER the DB write succeeds.
- Use `ApplicationEventPublisher.publishEvent(event)`.
- Events are immutable: all fields set in constructor, no setters. Use `@Getter` only.
- All event classes extend `ApplicationEvent` and live in `shared-events` module.
- Consume with `@EventListener` in dedicated `*EventHandler` classes; wrap in try/catch and log errors.
- Include all fields consumers need — consumers MUST NOT call back to the publisher.

## Performance

- Index frequently queried columns (`userId`, `claimId`, `status`).
- Use `Pageable` on all list endpoints — never return unbounded results.
- Use `@EntityGraph` or `JOIN FETCH` to prevent N+1 queries.
- Tune HikariCP pool size in `application.yml`.

## Class Size Limits

| Class Type | Max Lines |
|-----------|-----------|
| Controller | 80 |
| Service Interface | 30 |
| Service Implementation | 200 |
| Entity | 100 |
| DTO | 50 |
| Repository | 30 |
| Exception | 20 |
| Configuration | 80 |
| Event Handler | 60 |
| Event Class | 50 |
| Utility | 80 |
| GlobalExceptionHandler | 100 |
| Unit Test | 200 |

If a class exceeds its limit, extract logic into helpers or smaller services.

## Javadoc & Comments

### Class-Level (Mandatory)

Every class MUST have a Javadoc with: one-line summary, `@author SecureClaims Team`, `@since 1.0`.

```java
/**
 * Handles user registration, authentication, and JWT token issuance.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
```

### Method-Level

Required on all public methods in service interfaces, service implementations, and controllers. Include `@param`, `@return`, and `@throws` tags.

### Inline Comments

- Explain **why**, not what.
- Place on the line ABOVE the code, preceded by a blank line.
- Required before: validation blocks, event publishing, complex conditionals, status transitions, external calls, and logical section boundaries.

### Anti-Patterns (Never Do)

- Stating the obvious (`// set x to 5`).
- Leaving commented-out code.
- End-of-line comments for long explanations.

## Dependencies

- All versions declared in parent POM `<dependencyManagement>` — no overrides in child modules.
- New deps must be added to parent POM first.
- Prefer Spring ecosystem libraries over external utilities.
- No unused deps — validate with `mvn dependency:analyze`.

## Git Standards

- Branch: `feature/{story-id}-{desc}`, `bugfix/{issue-id}-{desc}`, `hotfix/{desc}`
- Commit: `{type}(scope): {description}` — types: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`
- Example: `feat(claims): add POST /claims endpoint`

## Code Review Checklist

Before considering code complete, verify:

- [ ] No business logic in controllers
- [ ] No entities in API responses
- [ ] All inputs validated with `@Valid`
- [ ] Constructor injection only (no `@Autowired` fields)
- [ ] Custom exceptions used (not generic RuntimeException)
- [ ] Swagger/OpenAPI annotations on all endpoints and DTOs
- [ ] Unit tests for new service methods
- [ ] No hardcoded secrets or magic numbers
- [ ] Class-level Javadoc present on every class
