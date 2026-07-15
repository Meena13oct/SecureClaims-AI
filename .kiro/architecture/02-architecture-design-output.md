# SecureClaims AI – Architecture Design

**System:** SecureClaims AI – Insurance Claims Processing System  
**Version:** 1.0  
**Date:** 2026-07-03  
**ADLC Step:** 2 – Architecture Design  
**Input:** `.kiro/outputs/01-requirements-output.md`  
**Technology Stack:** Java 17, Spring Boot 3.x, PostgreSQL, Spring Security + JWT, Spring Boot Events (in-memory)

---

## Table of Contents

1. [Architecture Diagram](#1-architecture-diagram)
2. [Requirements → Service Mapping](#2-requirements--service-mapping)
3. [Event Flow Sequence](#3-event-flow-sequence)
4. [Service Interaction Design](#4-service-interaction-design)
5. [Design Decisions & Justification](#5-design-decisions--justification)
6. [Security Architecture](#6-security-architecture)

---

## 1. Architecture Diagram

### 1.1 System Context Diagram (C4 Level 1)

```
╔══════════════════════════════════════════════════════════════════════════╗
║                        SecureClaims AI Platform                         ║
║                                                                          ║
║   ┌─────────────┐   REST/HTTPS   ┌──────────────────────────────────┐   ║
║   │    Guest    │ ─────────────► │  POST /auth/register             │   ║
║   │(Unauthenticated)             │  POST /auth/login                │   ║
║   └─────────────┘                └──────────────────────────────────┘   ║
║                                                                          ║
║   ┌─────────────┐   REST/HTTPS   ┌──────────────────────────────────┐   ║
║   │    User     │ ─────────────► │  POST /claims                    │   ║
║   │  (JWT Role: │                │  GET  /claims                    │   ║
║   │   USER)     │                │  POST /claims/{id}/documents     │   ║
║   └─────────────┘                │  GET  /auth/me                   │   ║
║                                  └──────────────────────────────────┘   ║
║                                                                          ║
║   ┌─────────────┐   REST/HTTPS   ┌──────────────────────────────────┐   ║
║   │    Admin    │ ─────────────► │  GET  /admin/claims              │   ║
║   │  (JWT Role: │                │  PUT  /admin/claims/{id}/status  │   ║
║   │   ADMIN)    │                │  GET  /admin/fraud/{claimId}     │   ║
║   └─────────────┘                │  GET  /admin/users               │   ║
║                                  │  GET  /admin/notifications/{uid} │   ║
║                                  └──────────────────────────────────┘   ║
╚══════════════════════════════════════════════════════════════════════════╝
```

---

### 1.2 Container Diagram (C4 Level 2) – Microservices Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          SecureClaims AI – Local Deployment                     │
│                                                                                 │
│  ┌──────────────────────┐         ┌──────────────────────┐                     │
│  │   Identity Service   │         │    Claims Service     │                     │
│  │  [Spring Boot 3.x]   │         │  [Spring Boot 3.x]    │                     │
│  │  Port: 8081          │         │  Port: 8082           │                     │
│  │                      │         │                       │                     │
│  │  • /auth/register    │         │  • /claims (CRUD)     │                     │
│  │  • /auth/login       │◄────────│  • /claims/{id}/docs  │                     │
│  │  • /auth/me          │  JWT    │  • /admin/claims      │                     │
│  │  • /admin/users      │ Verify  │                       │                     │
│  │                      │         │  Publishes:           │                     │
│  │  Spring Security     │         │  • ClaimCreatedEvent  │                     │
│  │  JWT Filter Chain    │         │  • ClaimStatusUpdated │                     │
│  │  BCrypt Password     │         │    Event              │                     │
│  │                      │         │                       │                     │
│  │  ┌────────────────┐  │         │  ┌─────────────────┐  │                     │
│  │  │  PostgreSQL DB  │  │         │  │  PostgreSQL DB   │  │                     │
│  │  │ Schema:identity│  │         │  │  Schema: claims  │  │                     │
│  │  │ • users        │  │         │  │  • claims        │  │                     │
│  │  │ • roles        │  │         │  │  • documents     │  │                     │
│  │  │ • user_roles   │  │         │  └─────────────────┘  │                     │
│  │  └────────────────┘  │         │                       │                     │
│  └──────────────────────┘         │  ┌─────────────────┐  │                     │
│                                   │  │  Local FS       │  │                     │
│                                   │  │ uploads/claims/ │  │                     │
│                                   │  │  {claimId}/     │  │                     │
│                                   │  └─────────────────┘  │                     │
│                                   └──────────────────────┘                     │
│                                                                                 │
│         Spring Boot In-Memory Event Bus (ApplicationEventPublisher)             │
│  ════════════════════════════════════════════════════════════════════           │
│            │ ClaimCreatedEvent          │ FraudAnalysisCompletedEvent           │
│            │ ClaimStatusUpdatedEvent    │                                       │
│            ▼                           ▼                                       │
│  ┌──────────────────────┐         ┌──────────────────────┐                     │
│  │  Fraud Detection Svc │         │  Notification Service │                     │
│  │  [Spring Boot 3.x]   │         │  [Spring Boot 3.x]    │                     │
│  │  Port: 8083          │         │  Port: 8084           │                     │
│  │                      │         │                       │                     │
│  │  Listens:            │         │  Listens:             │                     │
│  │  • ClaimCreatedEvent │         │  • ClaimCreatedEvent  │                     │
│  │                      │         │  • FraudAnalysis      │                     │
│  │  Rule Engine:        │         │    CompletedEvent     │                     │
│  │  • Amount scoring    │         │  • ClaimStatusUpdated │                     │
│  │  • Policy age check  │         │    Event              │                     │
│  │  • History scoring   │         │                       │                     │
│  │                      │         │  Delivers via:        │                     │
│  │  Publishes:          │         │  • Simulated Email    │                     │
│  │  • FraudAnalysis     │         │    (SLF4J console)    │                     │
│  │    CompletedEvent    │         │  • Simulated SMS      │                     │
│  │                      │         │    (SLF4J console)    │                     │
│  │  • /admin/fraud/     │         │  • /admin/            │                     │
│  │    {claimId}         │         │    notifications/     │                     │
│  │                      │         │    {userId}           │                     │
│  │  ┌────────────────┐  │         │                       │                     │
│  │  │  PostgreSQL DB  │  │         │  ┌─────────────────┐  │                     │
│  │  │  Schema: fraud  │  │         │  │  PostgreSQL DB   │  │                     │
│  │  │  • fraud_       │  │         │  │ Schema:          │  │                     │
│  │  │    analyses     │  │         │  │ notifications    │  │                     │
│  │  └────────────────┘  │         │  │ • notifications  │  │                     │
│  └──────────────────────┘         │  └─────────────────┘  │                     │
│                                   └──────────────────────┘                     │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────┐       │
│  │              Spring Boot Actuator  (/actuator/health)               │       │
│  │              OpenAPI / Swagger UI  (/swagger-ui.html)               │       │
│  │              SLF4J + Logback (structured logging)                   │       │
│  └─────────────────────────────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

### 1.3 Component Diagram (C4 Level 3) – Internal Layer Structure per Service

Each service follows the same layered internal architecture:

```
┌─────────────────────────────────────────────────────┐
│                  Microservice (e.g., Claims)         │
│                                                     │
│  ┌─────────────────────────────────────────────┐   │
│  │           Controller Layer                   │   │
│  │  @RestController  →  REST API endpoints      │   │
│  │  @RequestMapping  →  Route definitions       │   │
│  │  Input validation via @Valid / BindingResult │   │
│  └───────────────────┬─────────────────────────┘   │
│                      │                             │
│  ┌───────────────────▼─────────────────────────┐   │
│  │            Service Layer                     │   │
│  │  @Service  →  Business logic                 │   │
│  │  ApplicationEventPublisher → publish events  │   │
│  │  @EventListener → consume events             │   │
│  │  @Transactional → transaction boundaries     │   │
│  └───────────────────┬─────────────────────────┘   │
│                      │                             │
│  ┌───────────────────▼─────────────────────────┐   │
│  │          Repository Layer                    │   │
│  │  @Repository (Spring Data JPA)               │   │
│  │  JpaRepository<Entity, UUID>                 │   │
│  │  Custom JPQL / named queries                 │   │
│  └───────────────────┬─────────────────────────┘   │
│                      │                             │
│  ┌───────────────────▼─────────────────────────┐   │
│  │          Database Layer                      │   │
│  │  PostgreSQL – dedicated schema               │   │
│  │  Hibernate ORM / DDL auto-management         │   │
│  │  Spring DataSource (HikariCP connection pool)│   │
│  └─────────────────────────────────────────────┘   │
│                                                     │
│  ┌─────────────────────────────────────────────┐   │
│  │       Cross-Cutting Concerns                 │   │
│  │  GlobalExceptionHandler (@ControllerAdvice)  │   │
│  │  JWT Security Filter (Identity Service only) │   │
│  │  SLF4J + Logback (all services)              │   │
│  │  Spring Boot Actuator (all services)         │   │
│  │  OpenAPI / Swagger (all services)            │   │
│  └─────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

---

### 1.4 Deployment Diagram – Local Environment

```
┌─────────────────────────────────── Developer Machine ────────────────────────────────────┐
│                                                                                           │
│   JVM Process 1              JVM Process 2              JVM Process 3    JVM Process 4   │
│  ┌──────────────┐           ┌──────────────┐           ┌─────────────┐  ┌─────────────┐  │
│  │  Identity    │           │   Claims     │           │   Fraud     │  │Notification │  │
│  │  Service     │           │   Service    │           │  Detection  │  │  Service    │  │
│  │  :8081       │           │   :8082      │           │  Service    │  │  :8084      │  │
│  └──────┬───────┘           └──────┬───────┘           │  :8083      │  └──────┬──────┘  │
│         │                          │                   └──────┬──────┘         │         │
│         │                          │                          │                │         │
│         └──────────┬───────────────┘                          │                │         │
│                    │                          ┌───────────────┘                │         │
│                    ▼                          ▼                                ▼         │
│         ┌──────────────────────────────────────────────────────────────────────┐        │
│         │              PostgreSQL Server (localhost:5432)                       │        │
│         │                                                                       │        │
│         │   DB: secureclaims                                                    │        │
│         │   ├── Schema: identity    (users, roles, user_roles)                  │        │
│         │   ├── Schema: claims      (claims, documents)                         │        │
│         │   ├── Schema: fraud       (fraud_analyses)                            │        │
│         │   └── Schema: notifications (notifications)                           │        │
│         └──────────────────────────────────────────────────────────────────────┘        │
│                                                                                           │
│         ┌──────────────────────────────────────────────────────────────────────┐        │
│         │              Local File System                                        │        │
│         │              uploads/claims/{claimId}/*.pdf                           │        │
│         └──────────────────────────────────────────────────────────────────────┘        │
│                                                                                           │
│         ┌──────────────────────────────────────────────────────────────────────┐        │
│         │  In-Memory Event Bus (Spring ApplicationContext – shared JVM context) │        │
│         │  ClaimCreatedEvent | FraudAnalysisCompletedEvent | ClaimStatusUpdated │        │
│         └──────────────────────────────────────────────────────────────────────┘        │
└───────────────────────────────────────────────────────────────────────────────────────────┘
```

> Note: All four services share the same JVM ApplicationContext when run as a single Spring Boot multi-module application, enabling in-memory event bus communication. Alternatively, each can be run as an independent process with the event bus replaced by a message broker for full isolation.



---

## 2. Requirements → Service Mapping

Every functional requirement from `01-requirements-output.md` is mapped to the responsible service and the specific component that implements it.

### 2.1 Identity Service

| Req ID | Requirement Summary | Component | Endpoint / Mechanism |
|--------|--------------------|-----------|-----------------------|
| FR-001 | User registration with unique email/username | `AuthController` → `UserService` | `POST /auth/register` |
| FR-002 | BCrypt password hashing | `UserService` → `PasswordEncoder` | BCrypt bean in `SecurityConfig` |
| FR-003 | Authenticate user with email/password | `AuthController` → `AuthService` | `POST /auth/login` |
| FR-004 | Issue signed JWT token on login | `JwtTokenProvider` | Called from `AuthService.login()` |
| FR-005 | JWT contains userId, username, roles, expiry | `JwtTokenProvider.generateToken()` | HMAC-SHA256, claims builder |
| FR-006 | Validate JWT on every protected request | `JwtAuthenticationFilter` | Spring Security filter chain |
| FR-007 | Support USER and ADMIN roles | `Role` entity + `UserDetails` | `user_roles` join table |
| FR-008 | ADMIN retrieves all users | `AdminUserController` | `GET /admin/users` |
| FR-009 | ADMIN deactivates/reactivates user | `AdminUserController` → `UserService` | `PUT /admin/users/{id}/status` |
| FR-010 | 401 for missing/invalid JWT | `JwtAuthenticationFilter` | Returns `HttpStatus.UNAUTHORIZED` |
| FR-011 | 403 for unauthorized role access | Spring Security `AccessDeniedException` | `@PreAuthorize` / `HttpSecurity` config |
| FR-012 | GET /auth/me returns current user profile | `AuthController` | `GET /auth/me` (JWT principal) |
| FR-013 | Store credentials in identity schema | `UserRepository` → PostgreSQL | `spring.jpa.properties.hibernate.default_schema=identity` |

---

### 2.2 Claims Service

| Req ID | Requirement Summary | Component | Endpoint / Mechanism |
|--------|--------------------|-----------|-----------------------|
| FR-014 | Authenticated USER submits a claim | `ClaimController` → `ClaimService` | `POST /claims` |
| FR-015 | Claim requires policy#, type, date, description, amount | `ClaimRequest` DTO + `@Valid` | Bean validation annotations |
| FR-016 | Assign UUID, set status SUBMITTED | `ClaimService.createClaim()` | UUID.randomUUID(), `ClaimStatus.SUBMITTED` |
| FR-017 | USER retrieves their own claims | `ClaimController` → `ClaimService` | `GET /claims` (filtered by userId from JWT) |
| FR-018 | USER retrieves specific claim by ID | `ClaimController` → `ClaimService` | `GET /claims/{id}` + ownership check |
| FR-019 | ADMIN retrieves all claims | `AdminClaimController` | `GET /admin/claims` |
| FR-020 | ADMIN updates claim status | `AdminClaimController` → `ClaimService` | `PUT /admin/claims/{id}/status` |
| FR-021 | Valid statuses: SUBMITTED → CLOSED | `ClaimStatus` enum | State machine validation in `ClaimService` |
| FR-022 | USER uploads PDF documents | `DocumentController` → `DocumentService` | `POST /claims/{id}/documents` (multipart) |
| FR-023 | Files stored in uploads/claims/{claimId}/ | `FileStorageService` | Java NIO `Files.copy()` to local path |
| FR-024 | Only metadata stored in DB | `Document` entity | Stores filename, path, MIME type only |
| FR-025 | Reject non-PDF/image uploads | `DocumentService` + `FileValidator` | MIME type check → 400 Bad Request |
| FR-026 | USER lists documents for a claim | `DocumentController` | `GET /claims/{id}/documents` |
| FR-027 | Publish ClaimCreatedEvent after persist | `ClaimService` → `ApplicationEventPublisher` | `publisher.publishEvent(new ClaimCreatedEvent(...))` |
| FR-028 | Publish ClaimStatusUpdatedEvent on status change | `ClaimService` → `ApplicationEventPublisher` | `publisher.publishEvent(new ClaimStatusUpdatedEvent(...))` |

---

### 2.3 Fraud Detection Service

| Req ID | Requirement Summary | Component | Endpoint / Mechanism |
|--------|--------------------|-----------|-----------------------|
| FR-029 | Evaluate claim on ClaimCreatedEvent | `FraudEventHandler` | `@EventListener ClaimCreatedEvent` |
| FR-030 | Assess risk: amount, policy age, history | `FraudRuleEngine` | Three-rule scoring calculation |
| FR-031 | Classify as LOW / MEDIUM / HIGH | `RiskLevel` enum | Score threshold mapping |
| FR-032 | Scoring rules (amounts, thresholds) | `FraudRuleEngine.calculateScore()` | Hard-coded configurable thresholds |
| FR-033 | Persist fraud analysis result | `FraudAnalysisRepository` | `FraudAnalysis` entity → fraud schema |
| FR-034 | Publish FraudAnalysisCompletedEvent | `FraudService` → `ApplicationEventPublisher` | After persist, publish event |
| FR-035 | ADMIN retrieves fraud result for claim | `AdminFraudController` | `GET /admin/fraud/{claimId}` |
| FR-036 | HIGH risk → auto REJECTED | `ClaimService` (listens FraudAnalysisCompletedEvent) | Status set to REJECTED if HIGH |
| FR-037 | LOW/MEDIUM → UNDER_REVIEW | `ClaimService` (listens FraudAnalysisCompletedEvent) | Status set to UNDER_REVIEW |

---

### 2.4 Notification Service

| Req ID | Requirement Summary | Component | Endpoint / Mechanism |
|--------|--------------------|-----------|-----------------------|
| FR-038 | Notify user on claim submitted | `NotificationEventHandler` | `@EventListener ClaimCreatedEvent` |
| FR-039 | Notify user on fraud analysis done | `NotificationEventHandler` | `@EventListener FraudAnalysisCompletedEvent` |
| FR-040 | Notify user on status change | `NotificationEventHandler` | `@EventListener ClaimStatusUpdatedEvent` |
| FR-041 | Simulated email/SMS via console logs | `ConsoleEmailSender`, `ConsoleSMSSender` | SLF4J `log.info()` formatted output |
| FR-042 | Persist notification records | `NotificationRepository` | `Notification` entity → notifications schema |
| FR-043 | ADMIN retrieves notification history | `AdminNotificationController` | `GET /admin/notifications/{userId}` |
| FR-044 | Abstraction for future real providers | `NotificationSender` interface | Strategy pattern — injectable implementations |

---

### 2.5 Non-Functional Requirements Mapping

| NFR ID | Requirement Summary | Architectural Component |
|--------|---------------------|------------------------|
| NFR-001 | All endpoints require JWT (except auth) | Spring Security `HttpSecurity` permitAll for /auth/** |
| NFR-002 | JWT HMAC-SHA256, 24h expiry | `JwtTokenProvider` – configurable via `application.yml` |
| NFR-003 | No plain-text passwords | BCryptPasswordEncoder bean |
| NFR-004 | Externalized secrets via env vars | `application.yml` using `${JWT_SECRET}`, `${DB_PASSWORD}` |
| NFR-005 | RBAC enforcement | `@PreAuthorize("hasRole('ADMIN')")` on admin endpoints |
| NFR-006 | Input sanitization | Bean Validation (`@NotNull`, `@Size`, `@Pattern`) |
| NFR-007 | File type/size validation | `FileValidator` component in DocumentService |
| NFR-008–010 | Performance targets | HikariCP connection pool; indexed DB columns on UUID PKs |
| NFR-011 | Health endpoints | Spring Boot Actuator `management.endpoints.web.exposure.include=health` |
| NFR-012 | Retry for failed events | `@Scheduled` job polls `failed_events` table |
| NFR-013 | Global exception handling | `GlobalExceptionHandler` (`@RestControllerAdvice`) |
| NFR-014 | Structured error logging | Logback `logback-spring.xml` with JSON pattern |
| NFR-015 | Independent deployable services | Separate Spring Boot main classes per service module |
| NFR-016 | OpenAPI documentation | `springdoc-openapi-starter-webmvc-ui` dependency per service |
| NFR-017 | Java conventions + Javadoc | Code standards enforced via Checkstyle Maven plugin |
| NFR-018 | Independent Maven modules | Parent `pom.xml` + child module `pom.xml` per service |
| NFR-019 | 80% unit test coverage | JUnit 5 + Mockito; Jacoco Maven plugin for coverage reports |
| NFR-020 | Integration tests | `@SpringBootTest` + H2 in-memory or Testcontainers |
| NFR-021 | Event unit tests | Dedicated test classes per event publisher/listener |
| NFR-022 | Docker-ready | `Dockerfile` per service module |
| NFR-023 | Schema isolation | Separate PostgreSQL schema per service, no cross-schema FK |
| NFR-024 | Future broker migration | `EventPublisher` abstraction wrapping `ApplicationEventPublisher` |



---

## 3. Event Flow Sequence

### 3.1 Sequence Diagram: User Submits a Claim (Happy Path)

```
  User          Claims Svc       Event Bus       Fraud Svc       Notification Svc      DB
   │                │                │               │                  │               │
   │ POST /claims   │                │               │                  │               │
   │ (JWT token)    │                │               │                  │               │
   │───────────────►│                │               │                  │               │
   │                │ validate JWT   │               │                  │               │
   │                │ validate input │               │                  │               │
   │                │────────────────────────────────────────────────────────────────►  │
   │                │                │               │                  │  INSERT claim │
   │                │◄───────────────────────────────────────────────────────────────── │
   │                │ status=SUBMITTED               │                  │               │
   │                │                │               │                  │               │
   │                │ publishEvent(ClaimCreatedEvent)│                  │               │
   │                │───────────────►│               │                  │               │
   │                │                │               │                  │               │
   │                │                │──────────────►│                  │               │
   │                │                │  ClaimCreated │                  │               │
   │                │                │  Event        │ run rule engine  │               │
   │                │                │               │────────────────────────────────► │
   │                │                │               │                  │ INSERT fraud_ │
   │                │                │               │◄──────────────────────────────── │
   │                │                │               │  analyses row    │               │
   │                │                │               │ publishEvent(FraudAnalysis       │
   │                │                │               │ CompletedEvent)  │               │
   │                │                │◄──────────────│                  │               │
   │                │                │               │                  │               │
   │                │◄───────────────│ FraudAnalysis │                  │               │
   │                │  CompletedEvent│               │                  │               │
   │                │ update status  │               │                  │               │
   │                │ (REJECTED or   │               │                  │               │
   │                │  UNDER_REVIEW) │               │                  │               │
   │                │────────────────────────────────────────────────────────────────►  │
   │                │                │               │                  │ UPDATE claim  │
   │                │◄───────────────────────────────────────────────────────────────── │
   │                │ publishEvent(ClaimStatusUpdatedEvent)             │               │
   │                │───────────────►│               │                  │               │
   │                │                │                                  │               │
   │                │                │─────────────────────────────────►│               │
   │                │                │  ClaimCreatedEvent               │               │
   │                │                │  (from step above, parallel)     │               │
   │                │                │                  send email/SMS  │               │
   │                │                │                  log to console  │               │
   │                │                │                  ────────────────────────────►   │
   │                │                │                  ◄────────────────────────────   │
   │                │                │                  INSERT notification             │
   │                │                │                                  │               │
   │                │                │─────────────────────────────────►│               │
   │                │                │  ClaimStatusUpdatedEvent         │               │
   │                │                │                  send email/SMS  │               │
   │                │                │                  log to console  │               │
   │                │                │                  ────────────────────────────►   │
   │                │                │                  ◄────────────────────────────   │
   │                │                │                  INSERT notification             │
   │                │                │                                  │               │
   │◄───────────────│                │               │                  │               │
   │ 201 Created    │                │               │                  │               │
   │ {claimId, status: SUBMITTED}    │               │                  │               │
```

---

### 3.2 Sequence Diagram: Admin Manually Updates Claim Status

```
  Admin         Claims Svc       Event Bus       Notification Svc        DB
   │                │                │                  │                 │
   │ PUT /admin/claims/{id}/status   │                  │                 │
   │ (JWT ADMIN role)│               │                  │                 │
   │───────────────►│                │                  │                 │
   │                │ validate JWT + ADMIN role          │                 │
   │                │ validate new status transition     │                 │
   │                │────────────────────────────────────────────────────►│
   │                │                │                  │   UPDATE claim  │
   │                │◄────────────────────────────────────────────────────│
   │                │ publishEvent(ClaimStatusUpdatedEvent)               │
   │                │───────────────►│                  │                 │
   │                │                │─────────────────►│                 │
   │                │                │ ClaimStatus      │ send email/SMS  │
   │                │                │ UpdatedEvent     │ log to console  │
   │                │                │                  │────────────────►│
   │                │                │                  │◄────────────────│
   │                │                │                  │ INSERT notif.   │
   │◄───────────────│                │                  │                 │
   │ 200 OK         │                │                  │                 │
   │ {claimId, newStatus}            │                  │                 │
```

---

### 3.3 Sequence Diagram: User Uploads Document

```
  User          Claims Svc       File System         DB
   │                │                 │               │
   │ POST /claims/{id}/documents      │               │
   │ (multipart/form-data, JWT)       │               │
   │───────────────►│                 │               │
   │                │ validate JWT    │               │
   │                │ validate ownership of claim     │
   │                │ validate MIME type (PDF/image)  │
   │                │                 │               │
   │                │ Files.copy()    │               │
   │                │────────────────►│               │
   │                │  uploads/claims/│               │
   │                │  {claimId}/file │               │
   │                │◄────────────────│               │
   │                │ store confirmed │               │
   │                │─────────────────────────────────►
   │                │                 │  INSERT document metadata
   │                │◄─────────────────────────────────
   │◄───────────────│                 │               │
   │ 201 Created    │                 │               │
   │ {documentId, filename, filePath} │               │
```

---

### 3.4 Sequence Diagram: User Authentication

```
  Guest/User    Identity Svc       DB              JWT
   │                │               │               │
   │ POST /auth/register            │               │
   │───────────────►│               │               │
   │                │ validate uniqueness            │
   │                │──────────────►│               │
   │                │ BCrypt hash password           │
   │                │ INSERT user + USER role        │
   │                │◄──────────────│               │
   │◄───────────────│               │               │
   │ 201 Created    │               │               │
   │                │               │               │
   │ POST /auth/login               │               │
   │───────────────►│               │               │
   │                │──────────────►│               │
   │                │ SELECT user by email           │
   │                │◄──────────────│               │
   │                │ BCrypt verify password         │
   │                │───────────────────────────────►
   │                │               │  generateToken│
   │                │               │  (userId,     │
   │                │               │   roles,      │
   │                │               │   24h expiry) │
   │                │◄───────────────────────────────
   │◄───────────────│               │               │
   │ 200 OK         │               │               │
   │ {"token": "eyJ..."}            │               │
```



---

## 4. Service Interaction Design

### 4.1 Inter-Service Communication Model

```
┌─────────────────────────────────────────────────────────────────┐
│              Communication Patterns Summary                      │
│                                                                 │
│  Pattern 1: Synchronous REST (Client ↔ Service)                 │
│  ┌──────────┐    HTTP/JSON     ┌───────────────────┐            │
│  │  Client  │ ──────────────► │  Any Microservice  │            │
│  │ (User /  │ ◄────────────── │  (Controller Layer)│            │
│  │  Admin)  │  Response JSON  └───────────────────┘            │
│  └──────────┘                                                   │
│                                                                 │
│  Pattern 2: Asynchronous In-Memory Events (Service ↔ Service)  │
│  ┌───────────────┐  publishEvent()  ┌──────────────────────┐   │
│  │ Claims Service│ ───────────────► │ Spring ApplicationEvent│  │
│  │               │                  │ Publisher (in-memory) │   │
│  └───────────────┘                  └──────────┬───────────┘   │
│                                                │               │
│                                    ┌───────────┼───────────┐   │
│                                    ▼           ▼           ▼   │
│                              ┌──────────┐ ┌────────┐ ┌──────┐  │
│                              │  Fraud   │ │Claims  │ │Notif.│  │
│                              │Detection │ │Service │ │ Svc  │  │
│                              │@EventList│ │@EventL.│ │@EventL│  │
│                              └──────────┘ └────────┘ └──────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 Event Contract Definitions

All domain events are plain Java objects (POJOs) extending `ApplicationEvent`. They are the formal contracts between services.

```
┌────────────────────────────────────────────────────────────────────────┐
│ ClaimCreatedEvent                                                       │
│ Publisher : Claims Service                                              │
│ Consumers : Fraud Detection Service, Notification Service              │
├────────────────────────────────────────────────────────────────────────┤
│ Fields: eventId(UUID), claimId(UUID), userId(UUID),                    │
│         policyNumber(String), claimType(String),                       │
│         claimedAmount(BigDecimal), policyAgeMonths(int),               │
│         submittedAt(LocalDateTime)                                     │
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│ FraudAnalysisCompletedEvent                                             │
│ Publisher : Fraud Detection Service                                     │
│ Consumers : Claims Service, Notification Service                       │
├────────────────────────────────────────────────────────────────────────┤
│ Fields: eventId(UUID), claimId(UUID), userId(UUID),                    │
│         riskScore(int), riskLevel(RiskLevel enum: LOW/MEDIUM/HIGH),    │
│         analysisNotes(String), analyzedAt(LocalDateTime)               │
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│ ClaimStatusUpdatedEvent                                                 │
│ Publisher : Claims Service                                              │
│ Consumers : Notification Service                                       │
├────────────────────────────────────────────────────────────────────────┤
│ Fields: eventId(UUID), claimId(UUID), userId(UUID),                    │
│         previousStatus(ClaimStatus), newStatus(ClaimStatus),           │
│         updatedBy(String), updatedAt(LocalDateTime)                    │
└────────────────────────────────────────────────────────────────────────┘
```

### 4.3 Security Interaction Design

```
┌──────────────────────────────────────────────────────────────────┐
│                    JWT Security Flow                              │
│                                                                  │
│  Every inbound request to protected endpoints:                   │
│                                                                  │
│  [1] Request arrives at JwtAuthenticationFilter                  │
│       │                                                          │
│       ▼                                                          │
│  [2] Extract "Authorization: Bearer <token>" header              │
│       │                                                          │
│       ▼                                                          │
│  [3] JwtTokenProvider.validateToken(token)                       │
│       ├── Verify HMAC-SHA256 signature                           │
│       ├── Check expiry timestamp                                 │
│       └── Parse userId, username, roles from claims             │
│       │                                                          │
│       ▼                                                          │
│  [4] Build UsernamePasswordAuthenticationToken                   │
│      Set into SecurityContextHolder                              │
│       │                                                          │
│       ▼                                                          │
│  [5] Request proceeds to Controller                              │
│      Spring Security evaluates @PreAuthorize annotations         │
│       │                                                          │
│       ├── Role: USER → access /claims/**, /auth/me              │
│       └── Role: ADMIN → access /admin/**                         │
│                                                                  │
│  On failure:                                                     │
│  [3a] Invalid token → 401 Unauthorized                           │
│  [5a] Insufficient role → 403 Forbidden                          │
└──────────────────────────────────────────────────────────────────┘
```

### 4.4 Data Isolation Design

```
┌──────────────────────────────────────────────────────────────────────┐
│           Schema-per-Service Data Isolation                           │
│                                                                      │
│  PostgreSQL: database = secureclaims                                 │
│                                                                      │
│  ┌────────────────────┐   ┌────────────────────┐                    │
│  │  Schema: identity  │   │   Schema: claims    │                    │
│  │  ● users           │   │   ● claims          │                    │
│  │  ● roles           │   │   ● documents       │                    │
│  │  ● user_roles      │   │                     │                    │
│  └────────────────────┘   └────────────────────┘                    │
│                                                                      │
│  ┌────────────────────┐   ┌────────────────────┐                    │
│  │   Schema: fraud    │   │Schema: notifications│                   │
│  │  ● fraud_analyses  │   │   ● notifications   │                    │
│  │                    │   │                     │                    │
│  └────────────────────┘   └────────────────────┘                    │
│                                                                      │
│  Rules:                                                              │
│  ✓ No cross-schema foreign keys in the database                      │
│  ✓ Cross-service references by UUID value only                       │
│  ✓ Each service configures its own DataSource + schema               │
│  ✓ Each service owns its Flyway / DDL migrations independently       │
│  ✓ Data consistency maintained through domain events, not joins      │
└──────────────────────────────────────────────────────────────────────┘
```

### 4.5 File Storage Interaction Design

```
┌──────────────────────────────────────────────────────────────────────┐
│                 Document Upload Flow                                  │
│                                                                      │
│  Client sends: POST /claims/{id}/documents                           │
│  Content-Type: multipart/form-data                                   │
│                                                                      │
│  DocumentController                                                  │
│       │                                                              │
│       ▼                                                              │
│  DocumentService.uploadDocument()                                    │
│       │                                                              │
│       ├── [1] Validate claim ownership (userId from JWT == claim.userId)
│       ├── [2] Validate file type: MIME must be application/pdf       │
│       │        or image/jpeg, image/png                              │
│       ├── [3] Validate file size: max 10MB                           │
│       │                                                              │
│       ▼                                                              │
│  FileStorageService.storeFile()                                      │
│       │                                                              │
│       ├── Generate UUID-based stored filename                        │
│       ├── Resolve path: uploads/claims/{claimId}/{uuid}.pdf          │
│       ├── Create directories if not exist (Files.createDirectories)  │
│       └── Copy MultipartFile stream to path (Files.copy)             │
│                                                                      │
│  DocumentRepository.save()                                           │
│       │                                                              │
│       └── Persist: originalFilename, storedFilename, filePath,       │
│           mimeType, fileSizeBytes, uploadedAt                        │
│                                                                      │
│  Returns: DocumentResponse DTO (no binary data)                      │
└──────────────────────────────────────────────────────────────────────┘
```

### 4.6 Retry Mechanism Design

```
┌──────────────────────────────────────────────────────────────────────┐
│              Event Retry Mechanism (@Scheduled)                       │
│                                                                      │
│  Table: failed_events                                                │
│  Columns: id, event_type, payload(JSON), retry_count,                │
│           last_attempt_at, created_at                                │
│                                                                      │
│  Flow:                                                               │
│  [1] @EventListener throws exception                                 │
│       │                                                              │
│       ▼                                                              │
│  [2] Catch block: persist failed event to failed_events table        │
│                                                                      │
│  [3] @Scheduled(fixedDelay = 30000) RetryScheduler runs every 30s   │
│       │                                                              │
│       ▼                                                              │
│  [4] Query: SELECT * FROM failed_events WHERE retry_count < 3        │
│       │                                                              │
│       ▼                                                              │
│  [5] Deserialize payload, re-publish ApplicationEvent                │
│       │                                                              │
│       ├── Success → delete from failed_events                        │
│       └── Failure → increment retry_count, update last_attempt_at   │
│                                                                      │
│  [6] Events with retry_count >= 3 → flagged as DEAD_LETTER          │
│      (logged at ERROR level, requires manual admin intervention)     │
└──────────────────────────────────────────────────────────────────────┘
```



---

## 5. Design Decisions & Justification

### 5.1 Microservices Architecture

**Decision:** Split the system into four independent Spring Boot services: Identity, Claims, Fraud Detection, and Notification.

**Justification:**
- Each service has a clearly distinct business domain and change frequency. Authentication changes do not require redeploying the claims logic, and vice versa.
- Independent deployability (NFR-015) is a stated requirement. Spring Boot's self-contained JAR model makes this straightforward without a container orchestrator.
- Schema-per-service isolation (NFR-023) prevents accidental coupling through shared database tables — a common failure mode in shared-schema designs.
- The boundary separation makes future extraction into fully independent processes (with a real message broker) a structural refactor rather than a redesign (NFR-024).

---

### 5.2 Spring Boot In-Memory Event Bus (ApplicationEventPublisher)

**Decision:** Use Spring's built-in `ApplicationEventPublisher` and `@EventListener` for all inter-service communication instead of an external message broker like Apache Kafka or RabbitMQ.

**Justification:**
- The system is explicitly scoped to a local deployment with no requirement for distributed messaging (per the technology stack in `01-requirements-output.md`).
- In-memory events remove operational complexity: no broker setup, no network partitions, no serialization format to maintain.
- Spring's event system is synchronous by default (can be made async with `@Async`), which gives deterministic, testable event delivery.
- The abstraction layer (`EventPublisher` wrapper around `ApplicationEventPublisher`) means that if the system needs to scale, a Kafka adapter can replace the wrapper with zero changes to service business logic (satisfies NFR-024).
- Trade-off acknowledged: in-memory events do not survive JVM restarts. The retry mechanism using `@Scheduled` + `failed_events` table mitigates lost events during transient failures (satisfies NFR-012).

---

### 5.3 Schema-per-Service Database Design

**Decision:** Use a single PostgreSQL server with four separate schemas (`identity`, `claims`, `fraud`, `notifications`) — one per service.

**Justification:**
- Full database-per-service (separate PostgreSQL instances) would require Docker Compose or multiple database processes for local development, adding unnecessary setup friction.
- Schema-per-service achieves the same logical isolation (no cross-schema FK constraints, independent DDL ownership) at a lower operational cost for a local deployment (NFR-023).
- Each service configures its own `DataSource` pointing to its own schema, so the isolation boundary is enforced at the application level.
- Migration to separate databases in a cloud environment would only require changing connection strings per service.

---

### 5.4 JWT-Based Stateless Authentication

**Decision:** Use HMAC-SHA256 signed JWTs for authentication and authorization instead of server-side sessions.

**Justification:**
- Stateless tokens mean no session store is needed, which simplifies the Identity Service and enables future horizontal scaling.
- JWT claims carry the user's roles directly, allowing each service's Spring Security filter to enforce RBAC without calling back to the Identity Service on every request — reducing inter-service coupling and latency.
- BCrypt password hashing (NFR-003) and environment-variable JWT secrets (NFR-004) ensure credentials are never exposed.
- Token expiry (default 24h, configurable) limits the window of exposure if a token is compromised.

---

### 5.5 Rule-Based Fraud Detection Engine

**Decision:** Implement fraud detection as a deterministic rule engine scoring three factors (amount, policy age, claim history) rather than a machine learning model.

**Justification:**
- The requirements explicitly specify a rule-based engine, not ML (per `01-requirements-output.md` — FR-030 to FR-032).
- Rule engines are fully deterministic, auditable, and testable — every scoring decision can be unit tested with exact inputs and expected outputs (satisfies NFR-019, NFR-021).
- No training data, model serving infrastructure, or Python runtime is required — keeping the stack purely Java/Spring Boot.
- Rules are expressed as configurable constants in `application.yml`, allowing threshold tuning without code changes.
- The `FraudRuleEngine` class is isolated behind an interface, making it replaceable with an ML-backed implementation in a future iteration without changing the event contract or service structure.

---

### 5.6 Local File System Storage for Documents

**Decision:** Store uploaded claim documents on the local file system under `uploads/claims/{claimId}/` rather than a cloud object store (S3) or database BLOB.

**Justification:**
- Explicitly required by the technology stack and FR-023/FR-024.
- Storing only metadata in the database avoids BLOB performance issues and keeps database size predictable.
- The `FileStorageService` is abstracted behind an interface (`DocumentStorageService`), allowing future swap to S3 or Azure Blob Storage without changing `DocumentService` business logic.
- For local development, this eliminates any dependency on cloud credentials or internet connectivity.

---

### 5.7 Simulated Notification Delivery

**Decision:** Implement notification delivery as console/log output via SLF4J rather than real email/SMS providers.

**Justification:**
- The requirements specify simulated delivery (FR-041) to avoid dependency on external services (SendGrid, Twilio) during development.
- The `NotificationSender` interface (FR-044) follows the Strategy pattern: `ConsoleEmailSender` and `ConsoleSMSSender` are the default implementations, easily replaced by real provider adapters through Spring's dependency injection.
- All notifications are persisted to the database regardless of delivery channel, so the audit trail is always accurate even in simulated mode (FR-042).

---

### 5.8 Maven Multi-Module Project Structure

**Decision:** Organize all four services as modules under a single Maven parent project.

**Justification:**
- Satisfies NFR-018 (independent `pom.xml` per service) while providing a single `mvn install` at the root to build everything.
- Shared dependencies (Spring Boot BOM, JWT library, Lombok) can be declared in the parent POM with version management, preventing version drift across services.
- Each module produces an independent executable JAR (`spring-boot-maven-plugin`), satisfying NFR-015 and NFR-022.

**Recommended Module Structure:**
```
secureclaims-ai/                    ← Parent Maven project
├── pom.xml                         ← Parent POM (dependency management)
├── identity-service/
│   ├── pom.xml
│   └── src/
├── claims-service/
│   ├── pom.xml
│   └── src/
├── fraud-detection-service/
│   ├── pom.xml
│   └── src/
├── notification-service/
│   ├── pom.xml
│   └── src/
└── shared-events/                  ← Shared event POJOs (optional module)
    ├── pom.xml
    └── src/
```

---

### 5.9 Port Allocation

| Service | Port | Swagger UI |
|---------|------|------------|
| Identity Service | 8081 | http://localhost:8081/swagger-ui.html |
| Claims Service | 8082 | http://localhost:8082/swagger-ui.html |
| Fraud Detection Service | 8083 | http://localhost:8083/swagger-ui.html |
| Notification Service | 8084 | http://localhost:8084/swagger-ui.html |

---

### 5.10 Fraud Detection Scoring Table Reference

| Factor | Condition | Points Added | Risk Weight |
|--------|-----------|-------------|------------|
| Claimed Amount | > $50,000 | +3 | High |
| Claimed Amount | $10,000 – $50,000 | +1 | Medium |
| Policy Age | < 6 months | +2 | Medium |
| Prior Claims (12 mo) | > 3 claims | +3 | High |

| Total Score | Risk Level | Automatic Action |
|-------------|------------|-----------------|
| 0 – 1 | LOW | → UNDER_REVIEW |
| 2 – 3 | MEDIUM | → UNDER_REVIEW |
| 4+ | HIGH | → REJECTED (overridable by ADMIN) |

---

## 6. Security Architecture

This section specifies the security requirements and design constraints for the SecureClaims AI system. All services SHALL conform to these security specifications.

---

### 6.1 Authentication Requirements

The system SHALL implement stateless token-based authentication with the following specifications:

- The system SHALL authenticate users via signed JWT (JSON Web Token) using HMAC-SHA256 (HS256) algorithm
- The system SHALL require users to authenticate with email and password via a dedicated login endpoint
- The system SHALL hash all user passwords using BCrypt with a minimum work factor of 10 rounds before persisting to the database
- The system SHALL never store plaintext passwords in any data store, log file, or API response
- The system SHALL issue JWT tokens containing the user's ID, username, assigned roles, issued-at timestamp, and expiry timestamp
- The system SHALL enforce a configurable token expiry (default: 24 hours) after which the token becomes invalid
- The system SHALL validate the JWT signature and expiry on every request to a protected endpoint via a security filter in the request chain
- The system SHALL reject requests with missing, expired, malformed, or tampered tokens with HTTP 401 Unauthorized
- The system SHALL transmit credentials only in HTTPS POST request bodies — never in URLs, query parameters, or headers other than Authorization

---

### 6.2 Authorization Requirements

The system SHALL enforce Role-Based Access Control (RBAC) with the following role definitions:

| Role | Access Scope |
|------|-------------|
| `ROLE_USER` | The system SHALL allow users to submit claims, upload documents, and view only their own claims, documents, and notifications |
| `ROLE_ADMIN` | The system SHALL allow administrators to view all claims, update claim statuses, view fraud analysis results, manage user accounts, and view all notifications |
| `ANONYMOUS` | The system SHALL allow unauthenticated access only to user registration and login endpoints |

The system SHALL enforce authorization at three layers:
1. **URL-level** — The security configuration SHALL restrict endpoint URL patterns by role
2. **Method-level** — Protected controller methods SHALL declare required roles via annotations
3. **Data-level** — Service methods SHALL verify that the authenticated user owns the requested resource before returning data

The system SHALL return HTTP 403 Forbidden when an authenticated user attempts to access a resource beyond their assigned role.

The system SHALL enforce resource ownership — users SHALL NOT be able to view, modify, or upload documents to claims belonging to other users.

---

### 6.3 Data Protection Requirements

#### 6.3.1 Data at Rest

- The system SHALL store user passwords only as one-way BCrypt hashes
- The system SHALL store the JWT signing secret exclusively in environment variables — never in source code, configuration files committed to version control, or database tables
- The system SHALL externalize all database credentials via environment variables
- The system SHALL store uploaded documents with system-generated UUID filenames to prevent unauthorized access via filename guessing
- The system SHALL isolate each service's data in a separate database schema with no cross-schema foreign key relationships

#### 6.3.2 Data in Transit

- The system SHALL enforce HTTPS (TLS 1.2 or higher) for all client-to-service communication in production environments
- The system SHALL encrypt database connections in production using SSL/TLS
- Inter-service event communication SHALL occur in-memory within the application context, requiring no network-level encryption

#### 6.3.3 Sensitive Data Exposure Prevention

- The system SHALL exclude password hashes from all API response payloads
- The system SHALL never log JWT tokens, passwords, or secret keys in application logs
- The system SHALL return generic error messages in API responses without exposing stack traces, internal paths, or implementation details
- The system SHALL never include sensitive data (tokens, passwords, secrets) in URL query parameters
- The system SHALL never expose JPA entity objects directly in API responses — all responses SHALL use dedicated DTO objects

---

### 6.4 Input Validation Requirements

The system SHALL validate all external input at multiple layers:

| Layer | Requirement |
|-------|------------|
| **API Gateway / Controller** | The system SHALL validate all request body fields using Bean Validation constraints (not-null, not-blank, email format, size limits, numeric ranges) before processing |
| **Service Layer** | The system SHALL enforce business rules including valid state transitions, resource ownership, and logical constraints |
| **Database Layer** | The system SHALL enforce NOT NULL, UNIQUE, and foreign key constraints at the schema level as a final safety net |
| **File Upload** | The system SHALL validate uploaded file MIME types (allowing only PDF, JPEG, PNG) and reject files exceeding 10MB |

#### 6.4.1 Injection Prevention

- The system SHALL use parameterized queries (via ORM/JPA) for all database operations — raw SQL string concatenation SHALL NOT be used
- The system SHALL use only named parameters in custom JPQL queries
- The system SHALL sanitize uploaded filenames by generating UUID-based stored names, storing original filenames only as metadata
- The system SHALL resolve file storage paths from a configured base directory and SHALL reject any path containing directory traversal sequences (`..`)

#### 6.4.2 Cross-Site Scripting (XSS) Prevention

- The system SHALL serve only JSON responses (no server-rendered HTML), eliminating reflected XSS vectors
- The system SHALL validate string inputs against expected patterns using regex constraints where applicable

---

### 6.5 Secrets Management Requirements

- The system SHALL externalize all secrets (JWT signing key, database passwords, API keys, SSH keys) as environment variables or encrypted secret stores
- The system SHALL never commit secrets to version control — the `.gitignore` file SHALL exclude all files containing secrets (`.env.properties`, `.env`)
- Application configuration files SHALL use environment variable placeholders with development-only defaults that are clearly marked as non-production values
- The JWT signing secret SHALL be a minimum of 256 bits (32 bytes) in length
- CI/CD pipeline secrets SHALL be stored in the platform's encrypted secrets management (e.g., GitHub Secrets) and never echoed in build logs
- The system SHALL support secret rotation without service downtime — rotating the JWT secret SHALL invalidate tokens naturally at their expiry boundary

---

### 6.6 Network Security Requirements

- The system SHALL restrict database access to the internal Docker network — the PostgreSQL port SHALL NOT be exposed to the public internet
- The system SHALL restrict SSH access (port 22) to authorized administrator IP addresses only via security group rules
- The system SHALL deploy services behind a security group that limits inbound traffic to required ports only
- The system SHALL support future deployment behind an Application Load Balancer (ALB) for TLS termination and DDoS mitigation
- The system SHALL use IAM Instance Profiles for AWS service access instead of long-lived access keys on EC2 instances

---

### 6.7 Security Headers and API Hardening Requirements

The system SHALL include the following security headers in all HTTP responses:

| Header | Required Value | Purpose |
|--------|---------------|---------|
| `X-Content-Type-Options` | `nosniff` | The system SHALL prevent MIME-type sniffing attacks |
| `X-Frame-Options` | `DENY` | The system SHALL prevent clickjacking by disabling iframe embedding |
| `Cache-Control` | `no-store` on authenticated responses | The system SHALL prevent caching of authenticated response data |
| `Content-Type` | `application/json` | The system SHALL explicitly declare response content type |

The system SHALL disable CORS in production (same-origin policy enforced). If CORS is required for specific consumers, the system SHALL whitelist only explicitly approved origins.

The system SHALL implement rate limiting on authentication endpoints to prevent brute-force attacks.

---

### 6.8 Threat Model — Required Mitigations (STRIDE)

The system SHALL address the following threat categories with corresponding security controls:

| Threat | Attack Scenario | Required Mitigation |
|--------|----------------|---------------------|
| **Spoofing** | Attacker crafts a fake JWT token | The system SHALL verify HMAC-SHA256 signatures on every token and reject unsigned or incorrectly signed tokens |
| **Spoofing** | Brute-force login attempts | The system SHALL use BCrypt (computationally expensive) for password verification and SHALL implement rate limiting on login endpoints |
| **Tampering** | Attacker modifies JWT claims to escalate roles | The system SHALL reject any token whose signature does not match the payload — modified tokens SHALL return 401 |
| **Tampering** | SQL injection via user input | The system SHALL use parameterized queries exclusively — no dynamic SQL construction from user input |
| **Tampering** | Path traversal in file uploads | The system SHALL generate server-side filenames and SHALL NOT use client-provided filenames in filesystem paths |
| **Repudiation** | User denies submitting a fraudulent claim | The system SHALL maintain immutable audit fields (createdAt, userId) on all entities and SHALL log all state-changing operations with correlation IDs |
| **Information Disclosure** | Passwords leaked in API responses | The system SHALL exclude password fields from all serialized API responses |
| **Information Disclosure** | Stack traces expose internal architecture | The system SHALL catch all exceptions centrally and return only generic error messages to clients |
| **Information Disclosure** | Secrets committed to source control | The system SHALL externalize all secrets and SHALL maintain `.gitignore` rules excluding secret-containing files |
| **Denial of Service** | Oversized file upload exhausts disk/memory | The system SHALL enforce a maximum upload size (10MB) at the web server level |
| **Denial of Service** | Unbounded database queries exhaust resources | The system SHALL require pagination parameters on all list endpoints and SHALL NOT return unbounded result sets |
| **Elevation of Privilege** | Regular user accesses admin-only endpoints | The system SHALL enforce role requirements at both URL and method levels — requests without required roles SHALL be rejected with 403 |
| **Elevation of Privilege** | User views/modifies another user's data | The system SHALL verify resource ownership in the service layer before returning or modifying data |

---

### 6.9 Security Monitoring and Audit Requirements

- The system SHALL log all successful and failed authentication attempts at INFO level, including the email address but never the password
- The system SHALL log all authorization failures (403 responses) at WARN level, including the endpoint, required role, and authenticated user ID
- The system SHALL generate a unique correlation ID for each inbound request and propagate it through all processing layers for traceability
- The system SHALL use structured logging (JSON format) with consistent fields: timestamp, service name, log level, correlation ID, and message
- The system SHALL expose health check endpoints (`/actuator/health`) for monitoring service availability
- The system SHALL configure container health checks and automatic restart policies to recover from transient failures
- The system SHALL NOT log sensitive data including JWT tokens, passwords, secret keys, or full credit card numbers

---

*Section 6 specifies security requirements for the SecureClaims AI system architecture. These requirements SHALL be evaluated during design reviews and security assessments.*

---

*Document generated as part of ADLC Step 2 – Architecture Design for SecureClaims AI.*  
*Input: `.kiro/outputs/01-requirements-output.md`*
