# SecureClaims AI – User Stories

**System:** SecureClaims AI – Insurance Claims Processing System
**Version:** 1.0 | **Date:** 2026-07-04
**Input:** `.kiro/architecture/02-architecture-design-output.md`
**Stack:** Java 17, Spring Boot 3.x, PostgreSQL, Spring Security + JWT, Spring Boot Events

---

## Sprint & Epic Overview

| Sprint | Sprint Name | Epic | Epic Name | Stories | Days | Duration |
|--------|-------------|------|-----------|---------|------|----------|
| Sprint 1 | Core Platform | Epic 1 | Foundation & Infrastructure | US-001, US-002 | Day 1 | 1 day |
| Sprint 1 | Core Platform | Epic 2 | Identity Service | US-003, US-004, US-005, US-006 | Day 2–3 | 2 days |
| Sprint 2 | Claims & Fraud | Epic 3 | Claims Service | US-007, US-008, US-009, US-010 | Day 4–5 | 2 days |
| Sprint 2 | Claims & Fraud | Epic 4 | Fraud Detection Service | US-011, US-012 | Day 6 | 1 day |
| Sprint 3 | Notifications & Polish | Epic 5 | Notification Service | US-013, US-014 | Day 7 | 1 day |
| Sprint 3 | Notifications & Polish | Epic 6 | Admin Endpoints | US-015, US-016 | Day 8 | 1 day |
| Sprint 3 | Notifications & Polish | Epic 7 | Cross-Cutting & Testing | US-017, US-018, US-019, US-020 | Day 9–10 | 2 days |
| | | | | **20 stories** | **Day 1–10** | **10 days** |

---

## Epic Summary

| Epic | Name | Sprint | Stories | Count | Duration | Depends On |
|------|------|--------|---------|-------|----------|------------|
| Epic 1 | Foundation & Infrastructure | Sprint 1 | US-001, US-002 | 2 | 1 day | — |
| Epic 2 | Identity Service | Sprint 1 | US-003 – US-006 | 4 | 2 days | Epic 1 |
| Epic 3 | Claims Service | Sprint 2 | US-007 – US-010 | 4 | 2 days | Epic 2 |
| Epic 4 | Fraud Detection Service | Sprint 2 | US-011, US-012 | 2 | 1 day | Epic 3 |
| Epic 5 | Notification Service | Sprint 3 | US-013, US-014 | 2 | 1 day | Epic 3, Epic 4 |
| Epic 6 | Admin Endpoints | Sprint 3 | US-015, US-016 | 2 | 1 day | Epic 4, Epic 5 |
| Epic 7 | Cross-Cutting & Testing | Sprint 3 | US-017 – US-020 | 4 | 2 days | All Epics |
| **Total** | | | | **20** | **10 days** | |

---

## Day-by-Day Schedule

| Day | Sprint | Epic | Story ID | Title | Duration |
|-----|--------|------|----------|-------|----------|
| Day 1 | Sprint 1 | Epic 1 | US-001 | Project Scaffold & Database Setup | 0.5 day |
| Day 1 | Sprint 1 | Epic 1 | US-002 | Shared Events Module | 0.5 day |
| Day 2 | Sprint 1 | Epic 2 | US-003 | User Registration | 0.5 day |
| Day 2 | Sprint 1 | Epic 2 | US-004 | User Login and JWT Issuance | 0.5 day |
| Day 3 | Sprint 1 | Epic 2 | US-005 | JWT Authentication Filter & Security Config | 0.5 day |
| Day 3 | Sprint 1 | Epic 2 | US-006 | Role-Based Access Control & User Profile | 0.5 day |
| Day 4 | Sprint 2 | Epic 3 | US-007 | Submit a Claim | 0.5 day |
| Day 4 | Sprint 2 | Epic 3 | US-008 | View Claims | 0.5 day |
| Day 5 | Sprint 2 | Epic 3 | US-009 | Admin: View and Update Claims | 0.5 day |
| Day 5 | Sprint 2 | Epic 3 | US-010 | Upload and List Documents | 0.5 day |
| Day 6 | Sprint 2 | Epic 4 | US-011 | Fraud Rule Engine & Analysis Persistence | 0.5 day |
| Day 6 | Sprint 2 | Epic 4 | US-012 | Automatic Claim Status Update from Fraud Result | 0.5 day |
| Day 7 | Sprint 3 | Epic 5 | US-013 | Notify on Claim Submitted and Fraud Analysed | 0.5 day |
| Day 7 | Sprint 3 | Epic 5 | US-014 | Notify on Status Change | 0.5 day |
| Day 8 | Sprint 3 | Epic 6 | US-015 | Admin: View Fraud Analysis and Notification History | 0.5 day |
| Day 8 | Sprint 3 | Epic 6 | US-016 | Admin: List All Users | 0.5 day |
| Day 9 | Sprint 3 | Epic 7 | US-017 | Global Exception Handler | 0.5 day |
| Day 9 | Sprint 3 | Epic 7 | US-018 | Swagger UI on All Services | 0.5 day |
| Day 10 | Sprint 3 | Epic 7 | US-019 | Unit Tests for Key Business Logic | 0.5 day |
| Day 10 | Sprint 3 | Epic 7 | US-020 | Actuator Health & Final Integration Check | 0.5 day |

---

# Detailed User Stories

---

## Epic 1 – Foundation & Infrastructure

**Sprint:** Sprint 1 | **Day:** 1 | **Duration:** 1 day | **Goal:** Set up Maven multi-module project, PostgreSQL schemas, shared event contracts, and base service scaffolds so all services can be built on top.

---

### US-001 – Project Scaffold & Database Setup

**As a** developer,
**I want** the Maven multi-module project created with all four service modules and PostgreSQL schemas initialised,
**So that** I have a working skeleton to build every service on top of.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | Parent `pom.xml` at project root with Java 17, Spring Boot 3.x BOM, modules: `identity-service`, `claims-service`, `fraud-detection-service`, `notification-service`, `shared-events` |
| 2 | Each service has its own `pom.xml`, a `@SpringBootApplication` main class, and `application.yml` |
| 3 | Ports configured — identity: 8081, claims: 8082, fraud: 8083, notification: 8084 |
| 4 | PostgreSQL database `secureclaims` with four schemas: `identity`, `claims`, `fraud`, `notifications` |
| 5 | Each service `application.yml` sets `spring.jpa.properties.hibernate.default_schema` to its own schema |
| 6 | `spring.jpa.hibernate.ddl-auto=update` so tables auto-create on first run |
| 7 | All four services return `{"status":"UP"}` on `GET /actuator/health` |

**Done when:** `mvn spring-boot:run` works for all four services with no startup errors.

---

### US-002 – Shared Events Module

**As a** developer,
**I want** a `shared-events` Maven module containing all domain event POJOs and enums,
**So that** Claims, Fraud Detection, and Notification services share the same event contracts without code duplication.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | `ClaimCreatedEvent` with fields: claimId, userId, policyNumber, claimType, claimedAmount, policyAgeMonths, submittedAt |
| 2 | `FraudAnalysisCompletedEvent` with fields: claimId, userId, riskScore, riskLevel, analysisNotes, analyzedAt |
| 3 | `ClaimStatusUpdatedEvent` with fields: claimId, userId, previousStatus, newStatus, updatedBy, updatedAt |
| 4 | `RiskLevel` enum: LOW, MEDIUM, HIGH |
| 5 | `ClaimStatus` enum: SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, CLOSED |
| 6 | All event classes extend `org.springframework.context.ApplicationEvent` |
| 7 | `claims-service`, `fraud-detection-service`, and `notification-service` each declare `shared-events` as a Maven dependency |

**Done when:** All three services compile with shared event classes on the classpath.

---


## Epic 2 – Identity Service

**Sprint:** Sprint 1 | **Days:** 2–3 | **Duration:** 2 days | **Goal:** Implement user registration, login, JWT issuance, security filter, and role-based access control. All other services depend on a working JWT before their secured endpoints can function.

---

### US-003 – User Registration

**As a** guest,
**I want** to register an account with my name, email, username, and password,
**So that** I can log in and submit insurance claims.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | `POST /auth/register` accepts: firstName, lastName, email, username, password |
| 2 | Returns `400 Bad Request` with field-level errors if any field is blank or email format is invalid |
| 3 | Returns `409 Conflict` if email or username already exists |
| 4 | Password stored as BCrypt hash — never stored as plain text |
| 5 | User saved to `identity.users` with USER role assigned in `identity.user_roles` |
| 6 | Returns `201 Created` with user details (no password field in response) |

**Done when:** Postman call to `/auth/register` creates a user row with a hashed password in the DB.

---

### US-004 – User Login and JWT Issuance

**As a** registered user,
**I want** to log in with my email and password and receive a JWT token,
**So that** I can authenticate my subsequent API requests.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | `POST /auth/login` accepts: email, password |
| 2 | Returns `200 OK` with `{ "token": "eyJ..." }` on valid credentials |
| 3 | JWT contains: userId, username, roles, issuedAt, 24-hour expiry |
| 4 | JWT signed with HMAC-SHA256 using secret from `app.jwt.secret` in `application.yml` |
| 5 | Returns `401 Unauthorized` on wrong email or password |
| 6 | BCrypt `matches()` used to verify the submitted password against the stored hash |

**Done when:** Returned token decoded at jwt.io shows correct userId, username, and roles.

---

### US-005 – JWT Authentication Filter & Security Config

**As a** developer,
**I want** a Spring Security JWT filter that protects all endpoints except `/auth/**`,
**So that** only requests with a valid token can reach secured endpoints.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | `JwtAuthenticationFilter` extends `OncePerRequestFilter` and reads `Authorization: Bearer <token>` header |
| 2 | Valid token → builds `UsernamePasswordAuthenticationToken` and sets it in `SecurityContextHolder` |
| 3 | Missing or invalid token → returns `401 Unauthorized` immediately |
| 4 | `/auth/register` and `/auth/login` are publicly accessible with no token required |
| 5 | `SessionCreationPolicy.STATELESS` configured — no server-side session created |
| 6 | Filter registered in Spring Security filter chain before `UsernamePasswordAuthenticationFilter` |

**Done when:** `GET /auth/me` without a token → 401; with a valid token → 200.

---

### US-006 – Role-Based Access Control & User Profile

**As a** developer,
**I want** USER and ADMIN roles enforced on endpoints and a current-user profile endpoint,
**So that** regular users cannot access admin routes and can view their own account details.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | USER role grants access to `/claims/**` and `GET /auth/me` |
| 2 | ADMIN role grants access to `/admin/**` |
| 3 | `GET /auth/me` returns userId, username, email, firstName, lastName, roles — resolved from JWT, no DB call |
| 4 | Any `/admin/**` request with a USER token returns `403 Forbidden` |
| 5 | `@PreAuthorize("hasRole('ADMIN')")` applied to all admin controllers |
| 6 | Roles embedded in JWT as `ROLE_USER` / `ROLE_ADMIN` for Spring Security compatibility |

**Done when:** USER token on `/admin/claims` → 403; ADMIN token on `/admin/claims` → 200.

---


## Epic 3 – Claims Service

**Sprint:** Sprint 2 | **Days:** 4–5 | **Duration:** 2 days | **Goal:** Implement the core claims domain — submit, view, admin management, document upload, and event publishing. Claims Service is the central event publisher that Fraud Detection and Notification depend on.

---

### US-007 – Submit a Claim

**As an** authenticated user,
**I want** to submit an insurance claim with policy details,
**So that** my claim is recorded and automatically evaluated for fraud.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | `POST /claims` requires a valid USER JWT |
| 2 | Required request fields: policyNumber, claimType, incidentDate, description, claimedAmount |
| 3 | Returns `400 Bad Request` with field-level errors on missing or invalid input |
| 4 | Claim saved to `claims.claims` with auto-generated UUID, `status=SUBMITTED`, userId from JWT, submittedAt timestamp |
| 5 | Returns `201 Created` with claimId, status, submittedAt |
| 6 | `ClaimCreatedEvent` published to the in-memory event bus after the claim is saved |

**Done when:** Submitting a claim creates a DB row with SUBMITTED status and the fraud service receives the `ClaimCreatedEvent`.

---

### US-008 – View Claims

**As an** authenticated user,
**I want** to see all my submitted claims and the full details of a specific claim,
**So that** I can track the status of my submissions.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | `GET /claims` returns all claims belonging to the authenticated user (filtered by userId from JWT) |
| 2 | `GET /claims/{id}` returns full details of a single claim |
| 3 | Returns `403 Forbidden` if the requested claim belongs to a different user |
| 4 | Returns `404 Not Found` if the claim ID does not exist |
| 5 | Both endpoints require a valid USER JWT |

**Done when:** User A cannot retrieve User B's claims — ownership check enforced.

---

### US-009 – Admin: View and Update Claims

**As an** admin,
**I want** to view all claims in the system and manually update a claim's status,
**So that** I can oversee and manage the claims review workflow.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | `GET /admin/claims` returns all claims regardless of user; requires ADMIN JWT |
| 2 | `PUT /admin/claims/{id}/status` accepts `{ "status": "APPROVED" }`; requires ADMIN JWT |
| 3 | Valid status transitions enforced: SUBMITTED → UNDER_REVIEW → APPROVED / REJECTED → CLOSED |
| 4 | Returns `400 Bad Request` for an invalid status transition |
| 5 | Returns `404 Not Found` if the claim does not exist |
| 6 | `ClaimStatusUpdatedEvent` published to the event bus after each successful update |
| 7 | Returns `403 Forbidden` for USER role tokens |

**Done when:** Admin moves a claim through its full lifecycle; an invalid transition returns 400.

---

### US-010 – Upload and List Documents

**As an** authenticated user,
**I want** to upload a PDF document to my claim and view the list of uploaded documents,
**So that** I can attach supporting evidence for my claim.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | `POST /claims/{id}/documents` accepts `multipart/form-data` with a `file` field; requires USER JWT |
| 2 | Only `application/pdf` MIME type accepted — returns `400` for all other file types |
| 3 | Maximum file size 10 MB — returns `400` if exceeded |
| 4 | Returns `403 Forbidden` if the claim belongs to another user |
| 5 | File saved to `uploads/claims/{claimId}/` on the local filesystem |
| 6 | Document metadata saved to `claims.documents`: originalFilename, storedFilename, filePath, mimeType, fileSizeBytes, uploadedAt |
| 7 | Returns `201 Created` with documentId, originalFilename, filePath, uploadedAt |
| 8 | `GET /claims/{id}/documents` returns the list of documents for a claim owned by the JWT user |

**Done when:** PDF upload returns 201 with metadata; uploading a `.txt` file returns 400.

---


## Epic 4 – Fraud Detection Service

**Sprint:** Sprint 2 | **Day:** 6 | **Duration:** 1 day | **Goal:** Implement the rule-based fraud scoring engine that triggers automatically on claim submission, persists results, and publishes a completion event that drives automatic claim status updates.

---

### US-011 – Fraud Rule Engine & Analysis Persistence

**As a** developer,
**I want** the Fraud Detection Service to automatically score a claim when it is submitted and save the result,
**So that** every claim has an auditable, consistent risk assessment.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | `FraudEventHandler` listens for `ClaimCreatedEvent` via `@EventListener` |
| 2 | Rule 1 — Claimed Amount: > $50,000 → +3 pts; $10,000–$50,000 → +1 pt |
| 3 | Rule 2 — Policy Age: < 6 months → +2 pts |
| 4 | Rule 3 — Prior Claims (last 12 months): > 3 claims → +3 pts |
| 5 | Total score mapped to RiskLevel: 0–1 = LOW, 2–3 = MEDIUM, 4+ = HIGH |
| 6 | Result saved to `fraud.fraud_analyses`: claimId, userId, riskScore, riskLevel, analysisNotes, analyzedAt |
| 7 | `analysisNotes` contains a readable breakdown of which rules contributed points |
| 8 | `FraudAnalysisCompletedEvent` published to the event bus after the result is saved |
| 9 | All scoring thresholds are configurable via `application.yml` |

**Done when:** Submitting a claim with amount > $50,000 produces a HIGH risk row in `fraud.fraud_analyses`.

---

### US-012 – Automatic Claim Status Update from Fraud Result

**As a** developer,
**I want** the Claims Service to listen for `FraudAnalysisCompletedEvent` and automatically update the claim status,
**So that** HIGH risk claims are immediately rejected and others move to review without admin intervention.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | `ClaimService` has an `@EventListener` for `FraudAnalysisCompletedEvent` |
| 2 | riskLevel = HIGH → claim status updated to REJECTED, updatedBy = "FRAUD_ENGINE" |
| 3 | riskLevel = LOW or MEDIUM → claim status updated to UNDER_REVIEW, updatedBy = "FRAUD_ENGINE" |
| 4 | Status update is `@Transactional` |
| 5 | `ClaimStatusUpdatedEvent` published after the status change (reuses logic from US-009) |

**Done when:** Submitting a high-value claim → claim status automatically = REJECTED in the DB.

---

## Epic 5 – Notification Service

**Sprint:** Sprint 3 | **Day:** 7 | **Duration:** 1 day | **Goal:** Implement event-driven notifications that listen to all three domain events, simulate email/SMS delivery via console logging, and persist a record of every notification sent.

---

### US-013 – Notify on Claim Submitted and Fraud Analysed

**As a** user,
**I want** to be notified when my claim is submitted and when fraud analysis completes,
**So that** I know the system has received and evaluated my claim.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | `NotificationEventHandler` has `@EventListener` methods for both `ClaimCreatedEvent` and `FraudAnalysisCompletedEvent` |
| 2 | For each event, a simulated email is logged: `[EMAIL] To: {userId} \| Subject: ... \| Body: ...` |
| 3 | For each event, a simulated SMS is logged: `[SMS] To: {userId} \| Message: ...` |
| 4 | One `Notification` record saved per channel (EMAIL and SMS) to `notifications.notifications` |
| 5 | Record contains: notificationId, userId, claimId, type, channel, message, sentAt |

**Done when:** Claim submission produces 4 console log lines (2 events × 2 channels) and 4 DB rows.

---

### US-014 – Notify on Status Change

**As a** user,
**I want** to be notified whenever my claim status changes,
**So that** I am always up to date without having to manually check the system.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | `NotificationEventHandler` has an `@EventListener` for `ClaimStatusUpdatedEvent` |
| 2 | Notification message includes the previous status and the new status |
| 3 | Simulated email and SMS logged to console |
| 4 | Two `Notification` records saved: type = "STATUS_UPDATED", channels EMAIL and SMS |
| 5 | Triggers for both admin manual status updates (US-009) and fraud engine auto-updates (US-012) |

**Done when:** A status change produces 2 console log lines and 2 new rows in `notifications.notifications`.

---


## Epic 6 – Admin Endpoints

**Sprint:** Sprint 3 | **Day:** 8 | **Duration:** 1 day | **Goal:** Expose admin-only endpoints to retrieve fraud analysis results, notification history, and the full user list for auditing and oversight.

---

### US-015 – Admin: View Fraud Analysis and Notification History

**As an** admin,
**I want** to retrieve the fraud analysis result for a claim and the notification history for a user,
**So that** I can audit risk decisions and verify communications were sent.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | `GET /admin/fraud/{claimId}` returns: claimId, riskScore, riskLevel, analysisNotes, analyzedAt |
| 2 | Returns `404 Not Found` if no fraud analysis exists for the given claimId |
| 3 | `GET /admin/notifications/{userId}` returns all notification records for the given userId |
| 4 | Each notification record includes: notificationId, type, channel, message, sentAt |
| 5 | Both endpoints require ADMIN JWT — return `403 Forbidden` for USER tokens |

**Done when:** Admin retrieves fraud result and notification history; USER token blocked with 403.

---

### US-016 – Admin: List All Users

**As an** admin,
**I want** to retrieve a list of all registered users,
**So that** I can audit who has access to the system.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | `GET /admin/users` returns all users in the system; requires ADMIN JWT |
| 2 | Each record includes: userId, username, email, roles |
| 3 | Returns `403 Forbidden` for USER tokens |

**Done when:** Admin retrieves full user list in one call; USER token blocked with 403.

---

## Epic 7 – Cross-Cutting Concerns & Testing

**Sprint:** Sprint 3 | **Days:** 9–10 | **Duration:** 2 days | **Goal:** Apply consistent error handling and API documentation across all services, write unit tests for core business logic, and perform a final end-to-end smoke test of the complete system.

---

### US-017 – Global Exception Handler

**As a** developer,
**I want** a `GlobalExceptionHandler` in every service that returns consistent structured JSON error responses,
**So that** all errors have the same shape regardless of which service or layer produces them.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | `@RestControllerAdvice` class present in each of the four services |
| 2 | `MethodArgumentNotValidException` → `400 Bad Request` with per-field validation messages |
| 3 | `ResourceNotFoundException` → `404 Not Found` |
| 4 | `AccessDeniedException` → `403 Forbidden` |
| 5 | Unhandled `Exception` → `500 Internal Server Error` without stack trace in response body |
| 6 | All error responses follow: `{ "timestamp", "status", "error", "message", "path" }` |

**Done when:** Submitting a claim with a missing field returns a structured `400` body — not a raw stack trace.

---

### US-018 – Swagger UI on All Services

**As a** developer,
**I want** each service to expose an interactive Swagger UI,
**So that** all API endpoints can be explored and manually tested without reading source code.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | `springdoc-openapi-starter-webmvc-ui` dependency added to all four services |
| 2 | Swagger UI accessible at `http://localhost:{port}/swagger-ui.html` on each service |
| 3 | JWT Bearer token security scheme configured — secured endpoints show a lock icon |
| 4 | Each controller method annotated with `@Operation(summary = "...")` |

**Done when:** All four Swagger UIs open in a browser and a secured API call works by pasting a token.

---

### US-019 – Unit Tests for Key Business Logic

**As a** developer,
**I want** unit tests for the fraud rule engine, claims service, and auth service,
**So that** core business rules are verified and regressions are caught early.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | `FraudRuleEngineTest`: one test per scoring rule at boundary values (below / at / above threshold) |
| 2 | `ClaimServiceTest`: create claim success, invalid status transition rejected, `ClaimCreatedEvent` published on create |
| 3 | `AuthServiceTest`: register success, duplicate email returns error, wrong password returns 401 |
| 4 | All tests use JUnit 5 + Mockito — no Spring context required (pure unit tests) |
| 5 | All tests pass with `mvn test`; minimum 10 meaningful test cases total |

**Done when:** `mvn test` passes green with at least 10 test cases covering the three service classes.

---

### US-020 – Actuator Health & Final Integration Check

**As a** developer,
**I want** health endpoints active on all services and a final end-to-end smoke test to pass,
**So that** I can confirm the complete system works together before submission.

| # | Acceptance Criteria |
|---|---------------------|
| 1 | `GET /actuator/health` returns `{"status":"UP"}` on all four services |
| 2 | Health endpoint is publicly accessible — no JWT required |
| 3 | Smoke test passes: register → login → submit claim → verify fraud result in DB → verify notification rows in DB |
| 4 | All four services start cleanly from a fresh database with no errors in startup logs |

**Done when:** Full claim lifecycle runs end-to-end with events flowing correctly across all four services.

---

## API Endpoints Reference

| Story | Method | Endpoint | Role | Description |
|-------|--------|----------|------|-------------|
| US-003 | POST | `/auth/register` | Public | Register a new user |
| US-004 | POST | `/auth/login` | Public | Login and receive JWT token |
| US-006 | GET | `/auth/me` | USER | Get current user profile |
| US-007 | POST | `/claims` | USER | Submit a new claim |
| US-008 | GET | `/claims` | USER | List own claims |
| US-008 | GET | `/claims/{id}` | USER | Get a specific claim |
| US-009 | GET | `/admin/claims` | ADMIN | List all claims |
| US-009 | PUT | `/admin/claims/{id}/status` | ADMIN | Update claim status |
| US-010 | POST | `/claims/{id}/documents` | USER | Upload PDF document to a claim |
| US-010 | GET | `/claims/{id}/documents` | USER | List documents for a claim |
| US-015 | GET | `/admin/fraud/{claimId}` | ADMIN | Get fraud analysis for a claim |
| US-015 | GET | `/admin/notifications/{userId}` | ADMIN | Get notification history for a user |
| US-016 | GET | `/admin/users` | ADMIN | List all registered users |
| US-020 | GET | `/actuator/health` | Public | Service health check (all services) |

---

## Service Port Reference

| Service | Port | Swagger UI URL |
|---------|------|----------------|
| Identity Service | 8081 | http://localhost:8081/swagger-ui.html |
| Claims Service | 8082 | http://localhost:8082/swagger-ui.html |
| Fraud Detection Service | 8083 | http://localhost:8083/swagger-ui.html |
| Notification Service | 8084 | http://localhost:8084/swagger-ui.html |

---

*Document generated as part of ADLC Step 3 – User Stories for SecureClaims AI.*
*Input: `.kiro/architecture/02-architecture-design-output.md`*
*Total: 20 user stories | 7 Epics | 3 Sprints | 10 days*
