# SecureClaims AI – API Design

**System:** SecureClaims AI – Insurance Claims Processing System
**Version:** 1.0 | **Date:** 2026-07-04
**Input:** `.kiro/architecture/03-user-stories-output.md`
**Standards:** `.kiro/steering/api-standards.md`
**Stack:** Java 17, Spring Boot 3.x, Spring Security + JWT, PostgreSQL

---

## API Design Principles

- RESTful, resource-based URL structure
- All APIs versioned: `/api/{service}/v1/`
- JSON request and response bodies
- Stateless — JWT passed via `Authorization: Bearer <token>` header
- Standard success and error response envelopes
- Bean Validation on all inputs (`@NotNull`, `@NotBlank`, `@Email`, `@Min`, `@Max`)
- Correlation ID generated per request for tracing
- Pagination via `?page=0&size=10` on list endpoints

---

## Standard Headers

```
Content-Type: application/json
Accept: application/json
Authorization: Bearer <JWT>          # Required on all secured endpoints
X-Correlation-ID: <uuid>             # Auto-generated per request
```

---

## Standard Response Envelopes

### Success Response

```json
{
  "timestamp": "2026-07-04T10:15:30Z",
  "status": 200,
  "message": "Request processed successfully",
  "data": {}
}
```

### Error Response

```json
{
  "timestamp": "2026-07-04T10:15:30Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input data",
  "path": "/api/claims/v1/claims"
}
```

### Validation Error Response (400)

```json
{
  "timestamp": "2026-07-04T10:15:30Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more fields failed validation",
  "path": "/api/identity/v1/auth/register",
  "fieldErrors": [
    { "field": "email", "message": "must be a valid email address" },
    { "field": "password", "message": "must not be blank" }
  ]
}
```

---

## HTTP Status Code Reference

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 | OK | Successful GET, PUT, PATCH |
| 201 | Created | Successful POST that creates a resource |
| 400 | Bad Request | Validation failure, invalid transition |
| 401 | Unauthorized | Missing or invalid JWT |
| 403 | Forbidden | Valid JWT but insufficient role |
| 404 | Not Found | Resource does not exist |
| 409 | Conflict | Duplicate resource (email, username) |
| 500 | Internal Server Error | Unhandled exception |

---

## Service Base URLs

| Service | Port | Base URL |
|---------|------|----------|
| Identity Service | 8081 | `http://localhost:8081/api/identity/v1` |
| Claims Service | 8082 | `http://localhost:8082/api/claims/v1` |
| Fraud Detection Service | 8083 | `http://localhost:8083/api/fraud/v1` |
| Notification Service | 8084 | `http://localhost:8084/api/notifications/v1` |

---

---

# Epic 1 – Foundation & Infrastructure

## US-001 – Project Scaffold & Database Setup

> No REST endpoints exposed. Infrastructure-only story.
> Actuator health check available on all services (see US-020).

---

## US-002 – Shared Events Module

> No REST endpoints exposed. Internal Maven module providing shared event POJOs and enums.

---

---

# Epic 2 – Identity Service

**Base URL:** `http://localhost:8081/api/identity/v1`

---

## US-003 – User Registration

### `POST /api/identity/v1/auth/register`

**Role:** Public (no JWT required)
**Description:** Register a new user account.

#### Request Body

```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane.doe@example.com",
  "username": "janedoe",
  "password": "SecureP@ss1"
}
```

#### Validation Rules

| Field | Constraint |
|-------|-----------|
| firstName | `@NotBlank` |
| lastName | `@NotBlank` |
| email | `@NotBlank`, `@Email` |
| username | `@NotBlank` |
| password | `@NotBlank`, `@Size(min=8)` |

#### Responses

**201 Created**
```json
{
  "timestamp": "2026-07-04T10:00:00Z",
  "status": 201,
  "message": "User registered successfully",
  "data": {
    "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "username": "janedoe",
    "email": "jane.doe@example.com",
    "firstName": "Jane",
    "lastName": "Doe",
    "roles": ["USER"]
  }
}
```

**400 Bad Request** – Validation failure (missing or invalid fields)

**409 Conflict** – Email or username already registered
```json
{
  "timestamp": "2026-07-04T10:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Email already in use",
  "path": "/api/identity/v1/auth/register"
}
```

---

## US-004 – User Login and JWT Issuance

### `POST /api/identity/v1/auth/login`

**Role:** Public (no JWT required)
**Description:** Authenticate with email and password; receive a signed JWT.

#### Request Body

```json
{
  "email": "jane.doe@example.com",
  "password": "SecureP@ss1"
}
```

#### Validation Rules

| Field | Constraint |
|-------|-----------|
| email | `@NotBlank`, `@Email` |
| password | `@NotBlank` |

#### Responses

**200 OK**
```json
{
  "timestamp": "2026-07-04T10:05:00Z",
  "status": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

> JWT payload contains: `userId`, `username`, `roles`, `iat`, `exp` (24-hour expiry).
> Signed with HMAC-SHA256 using `app.jwt.secret`.

**401 Unauthorized** – Wrong email or password

---

## US-005 – JWT Authentication Filter & Security Config

> No REST endpoints. Spring Security filter configuration story.
> Enforces `Authorization: Bearer <token>` header on all routes except `/api/identity/v1/auth/**`.

---

## US-006 – Role-Based Access Control & User Profile

### `GET /api/identity/v1/auth/me`

**Role:** USER (JWT required)
**Description:** Return the profile of the currently authenticated user, resolved from the JWT — no DB call.

#### Headers

```
Authorization: Bearer <JWT>
```

#### Responses

**200 OK**
```json
{
  "timestamp": "2026-07-04T10:10:00Z",
  "status": 200,
  "message": "Request processed successfully",
  "data": {
    "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "username": "janedoe",
    "email": "jane.doe@example.com",
    "firstName": "Jane",
    "lastName": "Doe",
    "roles": ["ROLE_USER"]
  }
}
```

**401 Unauthorized** – Missing or invalid JWT

---

---

# Epic 3 – Claims Service

**Base URL:** `http://localhost:8082/api/claims/v1`

---

## US-007 – Submit a Claim

### `POST /api/claims/v1/claims`

**Role:** USER (JWT required)
**Description:** Submit a new insurance claim. Publishes `ClaimCreatedEvent` after save.

#### Headers

```
Authorization: Bearer <JWT>
Content-Type: application/json
```

#### Request Body

```json
{
  "policyNumber": "POL-2026-00123",
  "claimType": "MEDICAL",
  "incidentDate": "2026-06-20",
  "description": "Hospitalisation due to surgery",
  "claimedAmount": 75000.00
}
```

#### Validation Rules

| Field | Constraint |
|-------|-----------|
| policyNumber | `@NotBlank` |
| claimType | `@NotBlank` |
| incidentDate | `@NotNull`, `@PastOrPresent` |
| description | `@NotBlank` |
| claimedAmount | `@NotNull`, `@Min(1)` |

#### Responses

**201 Created**
```json
{
  "timestamp": "2026-07-04T11:00:00Z",
  "status": 201,
  "message": "Claim submitted successfully",
  "data": {
    "claimId": "c1d2e3f4-a5b6-7890-abcd-ef1234567890",
    "status": "SUBMITTED",
    "submittedAt": "2026-07-04T11:00:00Z"
  }
}
```

**400 Bad Request** – Validation failure

**401 Unauthorized** – Missing or invalid JWT

---

## US-008 – View Claims

### `GET /api/claims/v1/claims`

**Role:** USER (JWT required)
**Description:** List all claims belonging to the authenticated user. Supports pagination.

#### Query Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | int | 0 | Page number (0-based) |
| size | int | 10 | Page size |

#### Example Request

```
GET /api/claims/v1/claims?page=0&size=10
Authorization: Bearer <JWT>
```

#### Responses

**200 OK**
```json
{
  "timestamp": "2026-07-04T11:05:00Z",
  "status": 200,
  "message": "Request processed successfully",
  "data": {
    "content": [
      {
        "claimId": "c1d2e3f4-a5b6-7890-abcd-ef1234567890",
        "policyNumber": "POL-2026-00123",
        "claimType": "MEDICAL",
        "status": "SUBMITTED",
        "claimedAmount": 75000.00,
        "submittedAt": "2026-07-04T11:00:00Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

**401 Unauthorized** – Missing or invalid JWT

---

### `GET /api/claims/v1/claims/{id}`

**Role:** USER (JWT required)
**Description:** Retrieve full details of a single claim. Ownership enforced — users can only access their own claims.

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| id | UUID | Claim ID |

#### Responses

**200 OK**
```json
{
  "timestamp": "2026-07-04T11:06:00Z",
  "status": 200,
  "message": "Request processed successfully",
  "data": {
    "claimId": "c1d2e3f4-a5b6-7890-abcd-ef1234567890",
    "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "policyNumber": "POL-2026-00123",
    "claimType": "MEDICAL",
    "incidentDate": "2026-06-20",
    "description": "Hospitalisation due to surgery",
    "claimedAmount": 75000.00,
    "status": "UNDER_REVIEW",
    "submittedAt": "2026-07-04T11:00:00Z"
  }
}
```

**401 Unauthorized** – Missing or invalid JWT

**403 Forbidden** – Claim belongs to a different user

**404 Not Found** – Claim ID does not exist

---

## US-009 – Admin: View and Update Claims

### `GET /api/claims/v1/admin/claims`

**Role:** ADMIN (JWT required)
**Description:** List all claims in the system regardless of owner. Supports pagination.

#### Query Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | int | 0 | Page number |
| size | int | 10 | Page size |

#### Example Request

```
GET /api/claims/v1/admin/claims?page=0&size=10
Authorization: Bearer <JWT (ADMIN)>
```

#### Responses

**200 OK**
```json
{
  "timestamp": "2026-07-04T11:10:00Z",
  "status": 200,
  "message": "Request processed successfully",
  "data": {
    "content": [
      {
        "claimId": "c1d2e3f4-a5b6-7890-abcd-ef1234567890",
        "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        "policyNumber": "POL-2026-00123",
        "claimType": "MEDICAL",
        "status": "SUBMITTED",
        "claimedAmount": 75000.00,
        "submittedAt": "2026-07-04T11:00:00Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

**401 Unauthorized** | **403 Forbidden** (USER role)

---

### `PUT /api/claims/v1/admin/claims/{id}/status`

**Role:** ADMIN (JWT required)
**Description:** Manually update the status of a claim. Publishes `ClaimStatusUpdatedEvent` on success.

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| id | UUID | Claim ID |

#### Request Body

```json
{
  "status": "APPROVED"
}
```

#### Valid Status Transitions

```
SUBMITTED → UNDER_REVIEW → APPROVED → CLOSED
                         → REJECTED → CLOSED
```

#### Responses

**200 OK**
```json
{
  "timestamp": "2026-07-04T11:15:00Z",
  "status": 200,
  "message": "Claim status updated successfully",
  "data": {
    "claimId": "c1d2e3f4-a5b6-7890-abcd-ef1234567890",
    "previousStatus": "UNDER_REVIEW",
    "newStatus": "APPROVED",
    "updatedBy": "admin@secureclaims.com",
    "updatedAt": "2026-07-04T11:15:00Z"
  }
}
```

**400 Bad Request** – Invalid status transition

**401 Unauthorized** | **403 Forbidden** (USER role) | **404 Not Found**

---

## US-010 – Upload and List Documents

### `POST /api/claims/v1/claims/{id}/documents`

**Role:** USER (JWT required)
**Description:** Upload a PDF document as supporting evidence for a claim.

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| id | UUID | Claim ID |

#### Request

```
Content-Type: multipart/form-data
Authorization: Bearer <JWT>

Form field: file (PDF, max 10 MB)
```

#### Validation Rules

| Rule | Behaviour |
|------|-----------|
| MIME type must be `application/pdf` | Returns `400` for other types |
| File size must not exceed 10 MB | Returns `400` if exceeded |
| Claim must belong to authenticated user | Returns `403` otherwise |

#### Responses

**201 Created**
```json
{
  "timestamp": "2026-07-04T11:20:00Z",
  "status": 201,
  "message": "Document uploaded successfully",
  "data": {
    "documentId": "d1e2f3a4-b5c6-7890-abcd-ef1234567890",
    "originalFilename": "claim-evidence.pdf",
    "filePath": "uploads/claims/c1d2e3f4-a5b6-7890-abcd-ef1234567890/claim-evidence.pdf",
    "uploadedAt": "2026-07-04T11:20:00Z"
  }
}
```

**400 Bad Request** – Invalid file type or size exceeded

**401 Unauthorized** | **403 Forbidden** (claim owned by another user) | **404 Not Found**

---

### `GET /api/claims/v1/claims/{id}/documents`

**Role:** USER (JWT required)
**Description:** List all documents uploaded to a specific claim owned by the authenticated user.

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| id | UUID | Claim ID |

#### Responses

**200 OK**
```json
{
  "timestamp": "2026-07-04T11:25:00Z",
  "status": 200,
  "message": "Request processed successfully",
  "data": [
    {
      "documentId": "d1e2f3a4-b5c6-7890-abcd-ef1234567890",
      "originalFilename": "claim-evidence.pdf",
      "filePath": "uploads/claims/c1d2e3f4.../claim-evidence.pdf",
      "mimeType": "application/pdf",
      "fileSizeBytes": 204800,
      "uploadedAt": "2026-07-04T11:20:00Z"
    }
  ]
}
```

**401 Unauthorized** | **403 Forbidden** | **404 Not Found**

---

---

# Epic 4 – Fraud Detection Service

**Base URL:** `http://localhost:8083/api/fraud/v1`

> Fraud Detection Service operates primarily through internal Spring Application Events
> (`ClaimCreatedEvent` → scoring → `FraudAnalysisCompletedEvent`).
> No public-facing endpoints are exposed from this service directly.
> Admin access to fraud results is routed through the Claims Service admin endpoints (see Epic 6).

---

## US-011 – Fraud Rule Engine & Analysis Persistence

> No REST endpoints. Internal `@EventListener` on `ClaimCreatedEvent`.
> Scoring rules configurable via `application.yml`.

**Scoring Rules Summary:**

| Rule | Condition | Points |
|------|-----------|--------|
| Claimed Amount | > $50,000 | +3 |
| Claimed Amount | $10,000 – $50,000 | +1 |
| Policy Age | < 6 months | +2 |
| Prior Claims (12 months) | > 3 claims | +3 |

**Risk Level Mapping:**

| Score | Risk Level |
|-------|------------|
| 0 – 1 | LOW |
| 2 – 3 | MEDIUM |
| 4+ | HIGH |

---

## US-012 – Automatic Claim Status Update from Fraud Result

> No REST endpoints. Internal `@EventListener` on `FraudAnalysisCompletedEvent`.

**Auto-Update Logic:**

| Risk Level | Claim Status Set To | Updated By |
|------------|---------------------|------------|
| HIGH | REJECTED | FRAUD_ENGINE |
| LOW / MEDIUM | UNDER_REVIEW | FRAUD_ENGINE |

---

---

# Epic 5 – Notification Service

**Base URL:** `http://localhost:8084/api/notifications/v1`

> Notification Service operates through internal Spring Application Events.
> No public-facing endpoints are exposed from this service directly.
> Admin access to notification history is routed through admin endpoints (see Epic 6).

---

## US-013 – Notify on Claim Submitted and Fraud Analysed

> No REST endpoints. Internal `@EventListener` on `ClaimCreatedEvent` and `FraudAnalysisCompletedEvent`.

**Notification Channels:** EMAIL, SMS (console-logged simulation)
**DB Records:** 2 records per event (1 per channel) → 4 total for submission + fraud analysis

---

## US-014 – Notify on Status Change

> No REST endpoints. Internal `@EventListener` on `ClaimStatusUpdatedEvent`.

**Notification Channels:** EMAIL, SMS (console-logged simulation)
**DB Records:** 2 records per status change event

---

---

# Epic 6 – Admin Endpoints

---

## US-015 – Admin: View Fraud Analysis and Notification History

### `GET /api/fraud/v1/admin/fraud/{claimId}`

**Role:** ADMIN (JWT required)
**Description:** Retrieve the fraud analysis result for a specific claim.

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| claimId | UUID | Claim ID |

#### Responses

**200 OK**
```json
{
  "timestamp": "2026-07-04T12:00:00Z",
  "status": 200,
  "message": "Request processed successfully",
  "data": {
    "claimId": "c1d2e3f4-a5b6-7890-abcd-ef1234567890",
    "riskScore": 5,
    "riskLevel": "HIGH",
    "analysisNotes": "Claimed amount > $50,000 (+3 pts); Policy age < 6 months (+2 pts)",
    "analyzedAt": "2026-07-04T11:01:00Z"
  }
}
```

**401 Unauthorized** | **403 Forbidden** (USER role) | **404 Not Found**

---

### `GET /api/notifications/v1/admin/notifications/{userId}`

**Role:** ADMIN (JWT required)
**Description:** Retrieve the full notification history for a specific user.

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| userId | UUID | User ID |

#### Query Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | int | 0 | Page number |
| size | int | 10 | Page size |

#### Responses

**200 OK**
```json
{
  "timestamp": "2026-07-04T12:05:00Z",
  "status": 200,
  "message": "Request processed successfully",
  "data": {
    "content": [
      {
        "notificationId": "n1o2p3q4-r5s6-7890-abcd-ef1234567890",
        "type": "CLAIM_SUBMITTED",
        "channel": "EMAIL",
        "message": "Your claim POL-2026-00123 has been submitted.",
        "sentAt": "2026-07-04T11:00:30Z"
      },
      {
        "notificationId": "n2o3p4q5-r6s7-8901-bcde-fg2345678901",
        "type": "CLAIM_SUBMITTED",
        "channel": "SMS",
        "message": "Your claim POL-2026-00123 has been submitted.",
        "sentAt": "2026-07-04T11:00:30Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 4,
    "totalPages": 1
  }
}
```

**401 Unauthorized** | **403 Forbidden** (USER role)

---

## US-016 – Admin: List All Users

### `GET /api/identity/v1/admin/users`

**Role:** ADMIN (JWT required)
**Description:** Retrieve a paginated list of all registered users.

#### Query Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | int | 0 | Page number |
| size | int | 10 | Page size |

#### Responses

**200 OK**
```json
{
  "timestamp": "2026-07-04T12:10:00Z",
  "status": 200,
  "message": "Request processed successfully",
  "data": {
    "content": [
      {
        "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        "username": "janedoe",
        "email": "jane.doe@example.com",
        "roles": ["ROLE_USER"]
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

**401 Unauthorized** | **403 Forbidden** (USER role)

---

---

# Epic 7 – Cross-Cutting Concerns

---

## US-017 – Global Exception Handler

> No REST endpoints. `@RestControllerAdvice` applied in all four services.

**Standard Error Shape (all services):**
```json
{
  "timestamp": "2026-07-04T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "One or more fields failed validation",
  "path": "/api/claims/v1/claims"
}
```

---

## US-018 – Swagger UI on All Services

> No REST endpoints. SpringDoc OpenAPI configuration.

| Service | Swagger UI URL |
|---------|---------------|
| Identity Service | http://localhost:8081/swagger-ui.html |
| Claims Service | http://localhost:8082/swagger-ui.html |
| Fraud Detection Service | http://localhost:8083/swagger-ui.html |
| Notification Service | http://localhost:8084/swagger-ui.html |

JWT Bearer token scheme configured — secured endpoints display a lock icon.

---

## US-019 – Unit Tests

> No REST endpoints. Unit test coverage story.

---

## US-020 – Actuator Health

### `GET /actuator/health`

**Role:** Public (no JWT required)
**Description:** Service liveness check. Available on all four services at their respective ports.

#### Responses

**200 OK**
```json
{
  "status": "UP"
}
```

---

---

# Complete API Endpoint Reference

| Story | Method | Base URL | Path | Role | Description |
|-------|--------|----------|------|------|-------------|
| US-003 | POST | `identity:8081` | `/api/identity/v1/auth/register` | Public | Register new user |
| US-004 | POST | `identity:8081` | `/api/identity/v1/auth/login` | Public | Login, get JWT |
| US-006 | GET | `identity:8081` | `/api/identity/v1/auth/me` | USER | Get own profile |
| US-007 | POST | `claims:8082` | `/api/claims/v1/claims` | USER | Submit a claim |
| US-008 | GET | `claims:8082` | `/api/claims/v1/claims` | USER | List own claims |
| US-008 | GET | `claims:8082` | `/api/claims/v1/claims/{id}` | USER | Get claim details |
| US-009 | GET | `claims:8082` | `/api/claims/v1/admin/claims` | ADMIN | List all claims |
| US-009 | PUT | `claims:8082` | `/api/claims/v1/admin/claims/{id}/status` | ADMIN | Update claim status |
| US-010 | POST | `claims:8082` | `/api/claims/v1/claims/{id}/documents` | USER | Upload PDF document |
| US-010 | GET | `claims:8082` | `/api/claims/v1/claims/{id}/documents` | USER | List claim documents |
| US-015 | GET | `fraud:8083` | `/api/fraud/v1/admin/fraud/{claimId}` | ADMIN | Get fraud analysis |
| US-015 | GET | `notifications:8084` | `/api/notifications/v1/admin/notifications/{userId}` | ADMIN | Get notification history |
| US-016 | GET | `identity:8081` | `/api/identity/v1/admin/users` | ADMIN | List all users |
| US-020 | GET | all services | `/actuator/health` | Public | Service health check |

---

*Document generated as part of ADLC Step 4 – API Design for SecureClaims AI.*
*Input: `.kiro/architecture/03-user-stories-output.md`*
*Standards: `.kiro/steering/api-standards.md`*
*Total: 14 REST endpoints across 4 services | Event-driven flows for Fraud and Notification services*
