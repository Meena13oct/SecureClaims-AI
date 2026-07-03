# SecureClaims AI – Requirements Specification

**System:** SecureClaims AI – Insurance Claims Processing System  
**Version:** 1.0  
**Date:** 2026-07-03  
**ADLC Step:** 1 – Requirements Generation  
**Technology Stack:** Java 17, Spring Boot 3.x, PostgreSQL, Spring Security + JWT, Spring Boot Events

---

## Table of Contents

1. [Functional Requirements](#1-functional-requirements)
2. [Non-Functional Requirements](#2-non-functional-requirements)
3. [System Actors](#3-system-actors)
4. [Service Responsibilities](#4-service-responsibilities)
5. [Event Flow Description](#5-event-flow-description)
6. [Data Entities Overview](#6-data-entities-overview)

---

## 1. Functional Requirements

### 1.1 Identity Service – Authentication & Authorization

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-001 | The system shall allow a new user to register with a unique email address, username, and password. | High |
| FR-002 | The system shall hash user passwords using BCrypt before persisting to the database. | High |
| FR-003 | The system shall authenticate registered users via a POST /auth/login endpoint accepting email and password. | High |
| FR-004 | The system shall issue a signed JWT access token upon successful authentication. | High |
| FR-005 | The JWT token shall contain the user's ID, username, roles, and expiry timestamp as claims. | High |
| FR-006 | The system shall validate JWT tokens on every protected API request via a Spring Security filter chain. | High |
| FR-007 | The system shall support two roles: USER and ADMIN. | High |
| FR-008 | The system shall allow an ADMIN to retrieve a list of all registered users. | Medium |
| FR-009 | The system shall allow an ADMIN to deactivate or reactivate a user account. | Medium |
| FR-010 | The system shall return a 401 Unauthorized response for requests with missing or invalid JWT tokens. | High |
| FR-011 | The system shall return a 403 Forbidden response when an authenticated user accesses a resource beyond their role. | High |
| FR-012 | The system shall expose a GET /auth/me endpoint returning the profile of the currently authenticated user. | Medium |
| FR-013 | The system shall store user credentials in a dedicated PostgreSQL schema (identity schema). | High |

### 1.2 Claims Service – Claim Lifecycle Management

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-014 | The system shall allow an authenticated USER to submit a new insurance claim via POST /claims. | High |
| FR-015 | A claim submission shall require: policy number, claim type, incident date, description, and claimed amount. | High |
| FR-016 | The system shall assign a unique claim ID and set initial status to SUBMITTED upon claim creation. | High |
| FR-017 | The system shall allow an authenticated USER to retrieve all their own submitted claims via GET /claims. | High |
| FR-018 | The system shall allow an authenticated USER to retrieve a specific claim by ID via GET /claims/{id}. | High |
| FR-019 | The system shall allow an ADMIN to retrieve all claims across all users via GET /admin/claims. | High |
| FR-020 | The system shall allow an ADMIN to manually update the status of any claim via PUT /admin/claims/{id}/status. | High |
| FR-021 | Valid claim statuses shall be: SUBMITTED, UNDER_REVIEW, FRAUD_CHECK, APPROVED, REJECTED, CLOSED. | High |
| FR-022 | The system shall allow an authenticated USER to upload PDF documents for a claim via POST /claims/{id}/documents. | High |
| FR-023 | Uploaded documents shall be stored in the local file system under uploads/claims/{claimId}/. | High |
| FR-024 | The system shall store only file metadata (filename, file path, MIME type, upload timestamp) in the database; not the file binary. | High |
| FR-025 | The system shall reject document uploads that are not PDF or supported image formats, returning a 400 Bad Request. | Medium |
| FR-026 | The system shall allow an authenticated USER to list all documents associated with their claim via GET /claims/{id}/documents. | Medium |
| FR-027 | The system shall publish a ClaimCreatedEvent after a claim is successfully persisted, triggering downstream processing. | High |
| FR-028 | The system shall publish a ClaimStatusUpdatedEvent whenever a claim's status changes. | High |

### 1.3 Fraud Detection Service – Rule-Based Risk Scoring

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-029 | The system shall evaluate every new claim for fraud risk upon receiving a ClaimCreatedEvent. | High |
| FR-030 | The fraud engine shall assess risk based on three factors: claimed amount, policy age, and user claim history. | High |
| FR-031 | The fraud engine shall classify each claim as LOW, MEDIUM, or HIGH risk based on the combined score. | High |
| FR-032 | Risk scoring rules shall be: amount > $50,000 adds high weight; policy age < 6 months adds medium weight; more than 3 prior claims in 12 months adds high weight. | High |
| FR-033 | The system shall persist the fraud analysis result (risk score, risk level, analysis timestamp) linked to the claim ID. | High |
| FR-034 | The system shall publish a FraudAnalysisCompletedEvent after completing the fraud analysis. | High |
| FR-035 | The system shall allow an ADMIN to retrieve the fraud analysis result for any claim via GET /admin/fraud/{claimId}. | Medium |
| FR-036 | Claims flagged as HIGH risk shall automatically transition to REJECTED status unless overridden by an ADMIN. | Medium |
| FR-037 | Claims flagged as LOW or MEDIUM risk shall transition to UNDER_REVIEW status for further processing. | High |

### 1.4 Notification Service – User Alerts

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-038 | The system shall send a notification to the user when their claim is successfully submitted (ClaimCreatedEvent). | High |
| FR-039 | The system shall send a notification to the user when fraud analysis is completed (FraudAnalysisCompletedEvent). | High |
| FR-040 | The system shall send a notification to the user whenever their claim status changes (ClaimStatusUpdatedEvent). | High |
| FR-041 | Notifications shall be delivered via simulated email and SMS output to the application console/logs. | High |
| FR-042 | The system shall persist notification records (type, recipient, message, timestamp, delivery status) in the database. | Medium |
| FR-043 | The system shall allow an ADMIN to retrieve notification history for any user via GET /admin/notifications/{userId}. | Low |
| FR-044 | The Notification Service shall be designed with an abstraction layer to allow future integration with real email/SMS providers (e.g., SendGrid, Twilio). | Low |



---

## 2. Non-Functional Requirements

### 2.1 Security

| ID | Requirement |
|----|-------------|
| NFR-001 | All REST API endpoints (except /auth/register and /auth/login) shall require a valid JWT token. |
| NFR-002 | JWT tokens shall be signed using HMAC-SHA256 and expire after a configurable duration (default: 24 hours). |
| NFR-003 | Passwords shall never be stored or logged in plain text; BCrypt hashing is mandatory. |
| NFR-004 | Sensitive configuration values (database credentials, JWT secret) shall be externalized via environment variables and never hardcoded. |
| NFR-005 | The system shall enforce role-based access control; USER role cannot access ADMIN endpoints. |
| NFR-006 | The system shall sanitize all user inputs to prevent SQL injection and other injection attacks. |
| NFR-007 | File uploads shall be validated for type and size before being written to the local file system. |

### 2.2 Performance

| ID | Requirement |
|----|-------------|
| NFR-008 | REST API endpoints shall respond within 500ms for read operations under normal load (single developer/test environment). |
| NFR-009 | Claim submission and fraud analysis shall complete end-to-end within 3 seconds under normal load. |
| NFR-010 | The system shall support at least 20 concurrent users in the local deployment environment. |

### 2.3 Reliability & Availability

| ID | Requirement |
|----|-------------|
| NFR-011 | Spring Boot Actuator health endpoints (/actuator/health) shall be exposed and return service health status. |
| NFR-012 | The system shall implement a simple retry mechanism using Spring Scheduler for failed in-memory event deliveries. |
| NFR-013 | All unhandled exceptions shall be caught by a global exception handler and return structured JSON error responses. |
| NFR-014 | The system shall log all application errors at ERROR level with stack traces using SLF4J + Logback. |

### 2.4 Maintainability

| ID | Requirement |
|----|-------------|
| NFR-015 | Each microservice shall be independently deployable as a standalone Spring Boot application. |
| NFR-016 | All REST APIs shall be documented using OpenAPI 3.0 / Swagger UI accessible at /swagger-ui.html. |
| NFR-017 | Code shall follow standard Java naming conventions and include Javadoc for all public service classes and methods. |
| NFR-018 | Each service shall maintain its own Maven module with independent pom.xml dependency management. |

### 2.5 Testability

| ID | Requirement |
|----|-------------|
| NFR-019 | Unit tests shall be written using JUnit 5 and Mockito, achieving a minimum of 80% code coverage for service layer classes. |
| NFR-020 | Integration tests shall use Spring Boot Test with an in-memory H2 database or Testcontainers for PostgreSQL. |
| NFR-021 | All event publishing and handling logic shall have dedicated unit tests verifying event payload correctness. |

### 2.6 Scalability & Portability

| ID | Requirement |
|----|-------------|
| NFR-022 | Each microservice shall be Docker-ready with a Dockerfile, enabling optional containerized local deployment. |
| NFR-023 | The database schema-per-service design shall ensure no cross-service table dependencies, supporting independent scaling. |
| NFR-024 | The Spring Boot Events (in-memory) communication model shall be designed for future migration to an external message broker (e.g., Apache Kafka) with minimal code changes. |



---

## 3. System Actors

| Actor | Type | Description |
|-------|------|-------------|
| **Guest** | External Human | An unauthenticated visitor who can only access the registration and login endpoints. No access to protected resources. |
| **User (Policyholder)** | External Human | An authenticated individual with the USER role. Can submit claims, upload documents, view their own claims and their status, and receive notifications. |
| **Admin** | External Human | An authenticated individual with the ADMIN role. Can view all claims, update claim statuses, view fraud analysis results, manage users, and retrieve notification histories. |
| **Identity Service** | Internal System | Handles user registration, authentication, JWT issuance and validation, and role management. Acts as the security authority for the platform. |
| **Claims Service** | Internal System | Manages the full claim lifecycle from submission through final resolution. Publishes domain events to trigger downstream services. |
| **Fraud Detection Service** | Internal System | An event-driven internal service that consumes ClaimCreatedEvent, performs rule-based risk scoring, persists results, and publishes FraudAnalysisCompletedEvent. |
| **Notification Service** | Internal System | Consumes domain events (ClaimCreatedEvent, FraudAnalysisCompletedEvent, ClaimStatusUpdatedEvent) and delivers simulated notifications to users via console/log output. |
| **File System** | External System | Local operating system file storage used to persist uploaded claim documents under uploads/claims/{claimId}/. |
| **PostgreSQL Database** | External System | Relational database providing isolated schema storage for each microservice. |

---

## 4. Service Responsibilities

### 4.1 Identity Service

**Purpose:** Central authentication and authorization authority for the platform.

**Responsibilities:**
- Expose `POST /auth/register` – accepts user registration payload, validates uniqueness, hashes password, persists user with default USER role.
- Expose `POST /auth/login` – validates credentials, generates and returns a signed JWT token.
- Expose `GET /auth/me` – returns the profile of the currently authenticated user (extracted from JWT).
- Provide a Spring Security JWT filter that intercepts all incoming requests, validates the token, and populates the SecurityContext.
- Manage user roles (USER, ADMIN) and enforce role-based access control across all services.
- Expose `GET /admin/users` – ADMIN only; returns paginated list of all registered users.
- Expose `PUT /admin/users/{id}/status` – ADMIN only; activates or deactivates a user account.
- Store all user and role data in the `identity` PostgreSQL schema.

**Technology:** Java 17, Spring Boot 3.x, Spring Security, JWT (jjwt library), Spring Data JPA, PostgreSQL.

---

### 4.2 Claims Service

**Purpose:** Core service managing the entire insurance claim lifecycle.

**Responsibilities:**
- Expose `POST /claims` – authenticated USER submits a new claim; assign unique ID, set status to SUBMITTED, persist, and publish `ClaimCreatedEvent`.
- Expose `GET /claims` – authenticated USER retrieves all their own claims with pagination support.
- Expose `GET /claims/{id}` – authenticated USER retrieves a specific claim; validates ownership.
- Expose `PUT /admin/claims/{id}/status` – ADMIN only; manually transitions claim to any valid status and publishes `ClaimStatusUpdatedEvent`.
- Expose `GET /admin/claims` – ADMIN only; retrieves all claims with filtering and pagination.
- Expose `POST /claims/{id}/documents` – authenticated USER uploads a PDF/image document; save file to `uploads/claims/{claimId}/`; persist metadata to database.
- Expose `GET /claims/{id}/documents` – authenticated USER lists all documents for a claim.
- Publish `ClaimCreatedEvent` after successful claim persistence.
- Publish `ClaimStatusUpdatedEvent` after any claim status transition.
- Listen for `FraudAnalysisCompletedEvent` to automatically update claim status based on fraud risk level.
- Store all claim and document data in the `claims` PostgreSQL schema.

**Technology:** Java 17, Spring Boot 3.x, Spring Web, Spring Data JPA, PostgreSQL, Spring ApplicationEventPublisher, local file system storage.

---

### 4.3 Fraud Detection Service

**Purpose:** Automated rule-based risk assessment engine for submitted claims.

**Responsibilities:**
- Listen for `ClaimCreatedEvent` published by the Claims Service.
- Retrieve relevant claim data: claimed amount, associated policy age, and the submitting user's claim history (count of claims in the past 12 months).
- Apply the risk scoring rule engine:
  - Claimed amount > $50,000 → adds HIGH weight (+3 points)
  - Claimed amount $10,000–$50,000 → adds MEDIUM weight (+1 point)
  - Policy age < 6 months → adds MEDIUM weight (+2 points)
  - User has > 3 claims in the past 12 months → adds HIGH weight (+3 points)
  - Total score 0–1 → LOW risk; 2–3 → MEDIUM risk; 4+ → HIGH risk
- Persist the fraud analysis record: claim ID, risk score, risk level (LOW/MEDIUM/HIGH), analysis notes, timestamp.
- Publish `FraudAnalysisCompletedEvent` with claim ID and risk level after analysis completes.
- Expose `GET /admin/fraud/{claimId}` – ADMIN only; returns the fraud analysis result for a specific claim.
- Store all fraud analysis data in the `fraud` PostgreSQL schema.

**Technology:** Java 17, Spring Boot 3.x, Spring ApplicationEventListener, Spring Data JPA, PostgreSQL.

---

### 4.4 Notification Service

**Purpose:** User communication layer that delivers status updates and alerts.

**Responsibilities:**
- Listen for `ClaimCreatedEvent` → send a "Claim Received" notification to the submitting user.
- Listen for `FraudAnalysisCompletedEvent` → send a "Claim Under Review" or "Claim Flagged" notification based on risk level.
- Listen for `ClaimStatusUpdatedEvent` → send a "Claim Status Updated" notification with the new status.
- Simulate email delivery by logging a formatted email message to the console via SLF4J.
- Simulate SMS delivery by logging a formatted SMS message to the console via SLF4J.
- Persist all notification records: recipient user ID, notification type, channel (EMAIL/SMS), message body, sent timestamp, delivery status (SENT/FAILED).
- Expose `GET /admin/notifications/{userId}` – ADMIN only; retrieves notification history for a user.
- Provide a `NotificationSender` interface to abstract delivery channels for future real provider integration.
- Store all notification data in the `notifications` PostgreSQL schema.

**Technology:** Java 17, Spring Boot 3.x, Spring ApplicationEventListener, Spring Data JPA, PostgreSQL, SLF4J + Logback.



---

## 5. Event Flow Description

The system uses Spring Boot's in-memory `ApplicationEventPublisher` / `@EventListener` mechanism for asynchronous inter-service communication. No external message broker is required.

### 5.1 Event: ClaimCreatedEvent

**Trigger:** A user successfully submits a new insurance claim.  
**Publisher:** Claims Service  
**Consumers:** Fraud Detection Service, Notification Service

**Payload:**
```json
{
  "eventId": "uuid",
  "claimId": "uuid",
  "userId": "uuid",
  "policyNumber": "string",
  "claimType": "string",
  "claimedAmount": "decimal",
  "policyAgeMonths": "integer",
  "submittedAt": "ISO-8601 timestamp"
}
```

**Flow:**
```
User → POST /claims
         │
         ▼
  Claims Service
  [Persist claim, status = SUBMITTED]
         │
         ▼
  Publish ClaimCreatedEvent
         │
         ├──► Fraud Detection Service
         │    [Run risk scoring engine]
         │    [Persist FraudAnalysis record]
         │    [Publish FraudAnalysisCompletedEvent]
         │
         └──► Notification Service
              [Log simulated email: "Your claim #<id> has been received."]
              [Persist Notification record]
```

---

### 5.2 Event: FraudAnalysisCompletedEvent

**Trigger:** Fraud Detection Service completes risk scoring for a claim.  
**Publisher:** Fraud Detection Service  
**Consumers:** Claims Service, Notification Service

**Payload:**
```json
{
  "eventId": "uuid",
  "claimId": "uuid",
  "userId": "uuid",
  "riskScore": "integer",
  "riskLevel": "LOW | MEDIUM | HIGH",
  "analysisNotes": "string",
  "analyzedAt": "ISO-8601 timestamp"
}
```

**Flow:**
```
Fraud Detection Service
[FraudAnalysisCompletedEvent published]
         │
         ├──► Claims Service
         │    [if riskLevel == HIGH → set claim status = REJECTED]
         │    [if riskLevel == LOW or MEDIUM → set claim status = UNDER_REVIEW]
         │    [Publish ClaimStatusUpdatedEvent]
         │
         └──► Notification Service
              [if HIGH: Log "Your claim has been flagged for review."]
              [if LOW/MEDIUM: Log "Your claim is under review."]
              [Persist Notification record]
```

---

### 5.3 Event: ClaimStatusUpdatedEvent

**Trigger:** Any claim status transition (by ADMIN action or automated fraud result).  
**Publisher:** Claims Service  
**Consumers:** Notification Service

**Payload:**
```json
{
  "eventId": "uuid",
  "claimId": "uuid",
  "userId": "uuid",
  "previousStatus": "string",
  "newStatus": "string",
  "updatedBy": "string (userId or SYSTEM)",
  "updatedAt": "ISO-8601 timestamp"
}
```

**Flow:**
```
Claims Service
[ClaimStatusUpdatedEvent published]
         │
         └──► Notification Service
              [Log simulated email: "Your claim #<id> status has changed to <newStatus>."]
              [Persist Notification record]
```

---

### 5.4 Complete End-to-End Event Flow

```
[1] User submits claim
        │
        ▼
[2] Claims Service persists claim (status: SUBMITTED)
        │
        ▼
[3] ClaimCreatedEvent published
        │
        ├── [4a] Notification Service → "Claim received" notification sent & persisted
        │
        └── [4b] Fraud Detection Service runs risk scoring
                  │
                  ▼
             [5] FraudAnalysisCompletedEvent published
                  │
                  ├── [6a] Claims Service updates status
                  │         HIGH → REJECTED (publishes ClaimStatusUpdatedEvent)
                  │         LOW/MEDIUM → UNDER_REVIEW (publishes ClaimStatusUpdatedEvent)
                  │
                  └── [6b] Notification Service → "Fraud analysis result" notification sent
                                │
                                ▼
                        [7] ClaimStatusUpdatedEvent consumed
                                │
                                └── Notification Service → "Status updated" notification sent
```

### 5.5 Retry Mechanism

Failed event handler executions shall be retried using a Spring `@Scheduled` job that polls a `failed_events` log table and re-publishes events that have not been successfully processed within a configurable retry window (default: 3 retries, 30-second interval).



---

## 6. Data Entities Overview

Each microservice owns its data exclusively. There are no cross-service foreign key constraints. References between services are by ID (UUID) only.

### 6.1 Identity Service – Schema: `identity`

#### Entity: `users`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK, NOT NULL | Unique user identifier |
| username | VARCHAR(50) | UNIQUE, NOT NULL | Login username |
| email | VARCHAR(100) | UNIQUE, NOT NULL | User email address |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt-hashed password |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | Account active status |
| created_at | TIMESTAMP | NOT NULL | Account creation timestamp |
| updated_at | TIMESTAMP | NOT NULL | Last update timestamp |

#### Entity: `roles`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK, NOT NULL | Unique role identifier |
| name | VARCHAR(20) | UNIQUE, NOT NULL | Role name: USER or ADMIN |

#### Entity: `user_roles` (Join Table)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| user_id | UUID | FK → users.id | Reference to user |
| role_id | UUID | FK → roles.id | Reference to role |

---

### 6.2 Claims Service – Schema: `claims`

#### Entity: `claims`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK, NOT NULL | Unique claim identifier |
| user_id | UUID | NOT NULL | Reference to submitting user (from identity service) |
| policy_number | VARCHAR(50) | NOT NULL | Insurance policy number |
| claim_type | VARCHAR(50) | NOT NULL | Type of claim (e.g., HEALTH, AUTO, PROPERTY) |
| incident_date | DATE | NOT NULL | Date the incident occurred |
| description | TEXT | NOT NULL | Detailed description of the claim |
| claimed_amount | DECIMAL(15,2) | NOT NULL | Amount claimed in USD |
| status | VARCHAR(30) | NOT NULL | Claim status (SUBMITTED, UNDER_REVIEW, FRAUD_CHECK, APPROVED, REJECTED, CLOSED) |
| created_at | TIMESTAMP | NOT NULL | Claim submission timestamp |
| updated_at | TIMESTAMP | NOT NULL | Last status update timestamp |

#### Entity: `documents`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK, NOT NULL | Unique document identifier |
| claim_id | UUID | FK → claims.id, NOT NULL | Associated claim |
| original_filename | VARCHAR(255) | NOT NULL | Original name of the uploaded file |
| stored_filename | VARCHAR(255) | NOT NULL | System-generated stored filename |
| file_path | VARCHAR(500) | NOT NULL | Full local file system path |
| mime_type | VARCHAR(100) | NOT NULL | File MIME type (e.g., application/pdf) |
| file_size_bytes | BIGINT | NOT NULL | File size in bytes |
| uploaded_at | TIMESTAMP | NOT NULL | Upload timestamp |

---

### 6.3 Fraud Detection Service – Schema: `fraud`

#### Entity: `fraud_analyses`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK, NOT NULL | Unique analysis record identifier |
| claim_id | UUID | UNIQUE, NOT NULL | Reference to the analyzed claim (from claims service) |
| user_id | UUID | NOT NULL | Reference to the claim submitter (from identity service) |
| claimed_amount | DECIMAL(15,2) | NOT NULL | Snapshot of claimed amount at analysis time |
| policy_age_months | INTEGER | NOT NULL | Policy age in months at the time of analysis |
| prior_claims_count | INTEGER | NOT NULL | Number of user claims in the prior 12 months |
| risk_score | INTEGER | NOT NULL | Calculated numeric risk score |
| risk_level | VARCHAR(10) | NOT NULL | Risk classification: LOW, MEDIUM, or HIGH |
| analysis_notes | TEXT | | Human-readable scoring rationale |
| analyzed_at | TIMESTAMP | NOT NULL | Timestamp of analysis completion |

---

### 6.4 Notification Service – Schema: `notifications`

#### Entity: `notifications`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK, NOT NULL | Unique notification identifier |
| user_id | UUID | NOT NULL | Reference to recipient user (from identity service) |
| claim_id | UUID | | Reference to related claim, if applicable |
| notification_type | VARCHAR(50) | NOT NULL | Type: CLAIM_RECEIVED, FRAUD_ANALYSIS_DONE, STATUS_UPDATED |
| channel | VARCHAR(10) | NOT NULL | Delivery channel: EMAIL or SMS |
| recipient_address | VARCHAR(255) | NOT NULL | Email address or phone number |
| subject | VARCHAR(255) | | Notification subject (for email) |
| message_body | TEXT | NOT NULL | Full notification message content |
| delivery_status | VARCHAR(10) | NOT NULL | Delivery status: SENT or FAILED |
| sent_at | TIMESTAMP | NOT NULL | Notification send timestamp |

---

### 6.5 Entity Relationship Summary

```
identity.users ──(user_id reference)──► claims.claims
identity.users ──(user_id reference)──► fraud.fraud_analyses
identity.users ──(user_id reference)──► notifications.notifications

claims.claims ──(claim_id reference)──► fraud.fraud_analyses
claims.claims ──(claim_id reference)──► notifications.notifications
claims.claims ──(claim_id FK)────────► claims.documents
```

> Note: Cross-service references are by UUID value only. No database-level foreign key constraints span across schemas. Data consistency is maintained at the application/event level.

---

## Appendix: Technology Stack Summary

| Layer | Technology |
|-------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Web Layer | Spring Web (REST APIs) |
| Security | Spring Security + JWT (jjwt) |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL (schema-per-service) |
| Event Bus | Spring Boot ApplicationEventPublisher (in-memory) |
| File Storage | Local file system – uploads/claims/{claimId}/ |
| API Docs | OpenAPI 3.0 / Swagger UI |
| Logging | SLF4J + Logback |
| Monitoring | Spring Boot Actuator |
| Testing | JUnit 5 + Mockito + Spring Boot Test |
| Build | Maven |
| Deployment | Local / Docker-ready |

---

*Document generated as part of ADLC Step 1 – Requirements Generation for SecureClaims AI.*
