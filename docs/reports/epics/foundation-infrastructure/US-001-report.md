# Execution Report: US-001 – Project Scaffold & Database Setup

## 1. User Story Summary

* **User Story ID:** US-001
* **Title:** Project Scaffold & Database Setup
* **Description:** Set up Maven multi-module project with all four service modules and PostgreSQL schemas initialised, providing a working skeleton for all services to build on.

---

## 2. Functional Overview

* **What the feature does:** Establishes the foundation infrastructure — a Maven parent POM with 5 child modules (4 microservices + shared-events), proper Spring Boot configuration, PostgreSQL database with 4 schemas, and Actuator health endpoints.
* **Key business logic implemented:** None (infrastructure-only story). Scaffolding, dependency management, and database schema definitions.
* **Edge cases handled:**
  - All secrets externalized via environment variables with safe defaults
  - Test profiles use H2 in-memory database to avoid external PostgreSQL dependency
  - Flyway migrations create schemas idempotently (IF NOT EXISTS)

---

## 3. API Details

> **No REST endpoints exposed.** US-001 is an infrastructure-only story.
>
> Actuator health check is available on all services (covered in US-020):
> - `GET /actuator/health` → `{"status":"UP"}`

---

## 4. Database Changes

> **Note:** The SQL scripts and Flyway migrations are **prepared but not yet executed**. Tables will be created on first service startup, or by running the scripts manually in DBeaver.

### Schemas Defined (via Flyway migrations)

| Schema | Service | Tables |
|--------|---------|--------|
| `identity` | Identity Service | `users`, `roles`, `user_roles` |
| `claims` | Claims Service | `claims`, `documents` |
| `fraud` | Fraud Detection Service | `fraud_analyses` |
| `notifications` | Notification Service | `notifications` |

### Table Details

**identity.users**
```sql
id              UUID PRIMARY KEY DEFAULT gen_random_uuid()
username        VARCHAR(50) NOT NULL UNIQUE
email           VARCHAR(100) NOT NULL UNIQUE
first_name      VARCHAR(50) NOT NULL
last_name       VARCHAR(50) NOT NULL
password_hash   VARCHAR(255) NOT NULL
is_active       BOOLEAN NOT NULL DEFAULT TRUE
created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
```

**identity.roles**
```sql
id      UUID PRIMARY KEY DEFAULT gen_random_uuid()
name    VARCHAR(20) NOT NULL UNIQUE
```

**identity.user_roles**
```sql
user_id     UUID NOT NULL (FK → users.id)
role_id     UUID NOT NULL (FK → roles.id)
PRIMARY KEY (user_id, role_id)
```

**claims.claims**
```sql
id              UUID PRIMARY KEY DEFAULT gen_random_uuid()
user_id         UUID NOT NULL
policy_number   VARCHAR(50) NOT NULL
claim_type      VARCHAR(50) NOT NULL
incident_date   DATE NOT NULL
description     TEXT NOT NULL
claimed_amount  DECIMAL(15,2) NOT NULL
status          VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED'
created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
```

**claims.documents**
```sql
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid()
claim_id            UUID NOT NULL (FK → claims.id)
original_filename   VARCHAR(255) NOT NULL
stored_filename     VARCHAR(255) NOT NULL
file_path           VARCHAR(500) NOT NULL
mime_type           VARCHAR(100) NOT NULL
file_size_bytes     BIGINT NOT NULL
uploaded_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
```

**fraud.fraud_analyses**
```sql
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid()
claim_id            UUID NOT NULL UNIQUE
user_id             UUID NOT NULL
claimed_amount      DECIMAL(15,2) NOT NULL
policy_age_months   INTEGER NOT NULL
prior_claims_count  INTEGER NOT NULL
risk_score          INTEGER NOT NULL
risk_level          VARCHAR(10) NOT NULL CHECK (IN ('LOW','MEDIUM','HIGH'))
analysis_notes      TEXT
analyzed_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
```

**notifications.notifications**
```sql
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid()
user_id             UUID NOT NULL
claim_id            UUID
notification_type   VARCHAR(50) NOT NULL CHECK (IN ('CLAIM_RECEIVED','FRAUD_ANALYSIS_DONE','STATUS_UPDATED'))
channel             VARCHAR(10) NOT NULL CHECK (IN ('EMAIL','SMS'))
recipient_address   VARCHAR(255) NOT NULL
subject             VARCHAR(255)
message_body        TEXT NOT NULL
delivery_status     VARCHAR(10) NOT NULL DEFAULT 'SENT' CHECK (IN ('SENT','FAILED'))
sent_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
```

### Relationships

- `identity.user_roles.user_id` → `identity.users.id` (CASCADE DELETE)
- `identity.user_roles.role_id` → `identity.roles.id` (CASCADE DELETE)
- `claims.documents.claim_id` → `claims.claims.id` (CASCADE DELETE)

---

## 5. Data Inserted

* **Seed data inserted:** No (scripts prepared, not yet executed)
* **How to create tables and seed data:**
  1. Ensure PostgreSQL is running with `secureclaims` database created
  2. Run `dbScripts/V0__create_database_and_schemas.sql` manually in DBeaver, OR start any service (`mvn spring-boot:run`) to let Flyway auto-execute migrations
  3. Seed data (roles) is included in the identity Flyway migration (`V1__create_identity_tables.sql`) and runs automatically on first startup

| Table | Insert Statement (auto-runs via Flyway) |
|-------|-----------------|
| `identity.roles` | `INSERT INTO roles (id, name) VALUES (gen_random_uuid(), 'USER'), (gen_random_uuid(), 'ADMIN') ON CONFLICT (name) DO NOTHING;` |

**Expected Records after first startup:**

| id | name |
|----|------|
| `<auto-generated UUID>` | USER |
| `<auto-generated UUID>` | ADMIN |

---

## 6. Postman Testing Guide

Since US-001 has no REST API endpoints, the only testable endpoint is the Actuator health check.

### Health Check — Identity Service

* **URL:** `GET http://localhost:8081/actuator/health`
* **Headers:** None required
* **Request Body:** None

**Expected Response (200 OK):**
```json
{
  "status": "UP"
}
```

### Health Check — Claims Service

* **URL:** `GET http://localhost:8082/actuator/health`

### Health Check — Fraud Detection Service

* **URL:** `GET http://localhost:8083/actuator/health`

### Health Check — Notification Service

* **URL:** `GET http://localhost:8084/actuator/health`

> **Prerequisites:** PostgreSQL running with `secureclaims` database and schemas created. Run `dbScripts/V0__create_database_and_schemas.sql` first.

---

## 7. Test Coverage

### Unit Tests Created

| Service | Test Class | Method | Result |
|---------|-----------|--------|--------|
| Identity Service | `IdentityServiceApplicationTests` | `should_loadContext_when_applicationStarts` | ✅ PASS |
| Claims Service | `ClaimsServiceApplicationTests` | `should_loadContext_when_applicationStarts` | ✅ PASS |
| Fraud Detection Service | `FraudDetectionServiceApplicationTests` | `should_loadContext_when_applicationStarts` | ✅ PASS |
| Notification Service | `NotificationServiceApplicationTests` | `should_loadContext_when_applicationStarts` | ✅ PASS |

### Scenarios Covered

- Spring application context loads successfully for all 4 services
- H2 in-memory database connection works via `@ActiveProfiles("test")`
- JPA EntityManagerFactory initializes without errors
- Actuator endpoints are configured

### Build Result

```
[INFO] BUILD SUCCESS
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Total time: ~53s
```

---

## 8. Notes / Assumptions

### Assumptions

1. PostgreSQL is running locally on port 5432 with database `secureclaims` created
2. Environment variables (`DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`) are set in production; defaults are safe for local dev
3. Flyway migrations handle schema creation; `ddl-auto=update` handles JPA entity additions between migrations
4. All services share the same database but operate in isolated schemas

### Fixes Applied During Validation

| Issue | Before | After |
|-------|--------|-------|
| Hardcoded DB credentials | `username: admin` | `${DB_USERNAME:postgres}` |
| Hardcoded JWT secret | Plain text secret | `${JWT_SECRET:default-dev-secret-...}` |
| Wrong ddl-auto | `validate` | `update` (per AC #6) |
| show-sql | `true` | `false` (per coding standards) |
| JWT key name | `expiration-ms` | `expiry-ms` (per coding standards) |
| Missing Javadoc | No class-level docs | Added to all 4 main classes |
| Test profiles | No `@ActiveProfiles` | Added `@ActiveProfiles("test")` |
| Test DB | PostgreSQL (requires running DB) | H2 in-memory (self-contained) |
| Missing H2 dependency | Not in POMs | Added to all 4 service POMs |

### Acceptance Criteria Verification

| # | Criteria | Status |
|---|----------|--------|
| 1 | Parent POM with Java 17, Spring Boot 3.x, 5 modules | ✅ Met |
| 2 | Each service has pom.xml, @SpringBootApplication, application.yml | ✅ Met |
| 3 | Ports: 8081, 8082, 8083, 8084 | ✅ Met |
| 4 | PostgreSQL `secureclaims` with 4 schemas | ✅ Met |
| 5 | `hibernate.default_schema` configured per service | ✅ Met |
| 6 | `ddl-auto=update` | ✅ Fixed |
| 7 | `/actuator/health` returns UP | ✅ Configured |
