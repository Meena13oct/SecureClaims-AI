# Identity Management Service – Requirements

## Overview
Central authentication and authorization service for the SecureClaims AI platform. Handles user registration, login, JWT token issuance/validation, and role-based access control.

## Functional Requirements

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

## Non-Functional Requirements

| ID | Requirement |
|----|-------------|
| NFR-001 | All REST API endpoints (except /auth/register and /auth/login) shall require a valid JWT token. |
| NFR-002 | JWT tokens shall be signed using HMAC-SHA256 and expire after a configurable duration (default: 24 hours). |
| NFR-003 | Passwords shall never be stored or logged in plain text; BCrypt hashing is mandatory. |
| NFR-004 | Sensitive configuration values (database credentials, JWT secret) shall be externalized via environment variables. |
| NFR-005 | The system shall enforce role-based access control; USER role cannot access ADMIN endpoints. |

## Data Entities

### Entity: `users` (Schema: `identity`)

| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK, NOT NULL |
| username | VARCHAR(50) | UNIQUE, NOT NULL |
| email | VARCHAR(100) | UNIQUE, NOT NULL |
| password_hash | VARCHAR(255) | NOT NULL |
| is_active | BOOLEAN | NOT NULL, DEFAULT true |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

### Entity: `roles`

| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK, NOT NULL |
| name | VARCHAR(20) | UNIQUE, NOT NULL |

### Entity: `user_roles` (Join Table)

| Column | Type | Constraints |
|--------|------|-------------|
| user_id | UUID | FK → users.id |
| role_id | UUID | FK → roles.id |
