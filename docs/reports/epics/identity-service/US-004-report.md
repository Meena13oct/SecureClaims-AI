# Execution Report — US-004: User Login and JWT Issuance

---

## 1. User Story Summary

| Field | Value |
|-------|-------|
| **Story ID** | US-004 |
| **Title** | User Login and JWT Issuance |
| **Epic** | Epic 2 – Identity Service |
| **Sprint** | Sprint 1 |
| **Description** | As a registered user, I want to log in with my email and password and receive a JWT token, so that I can authenticate my subsequent API requests. |

---

## 2. Functional Overview

### What the feature does

Authenticates a registered user using their email and password, and returns a signed JWT token that can be used to access secured endpoints across all microservices.

### Key business logic implemented

- User lookup by email address via `UserRepository.findByEmail()`
- Password verification using BCrypt `matches()` against the stored hash
- JWT token generation containing userId (as subject), username, roles (prefixed with `ROLE_`), issuedAt, and 24-hour expiry
- Token signed with HMAC-SHA256 using the secret from `app.jwt.secret` configuration
- Generic error message ("Invalid email or password") for both wrong email and wrong password to prevent user enumeration

### Edge cases handled

- Non-existent email → 401 Unauthorized (no user enumeration leak)
- Correct email but wrong password → 401 Unauthorized
- Blank email or password → 400 Bad Request with field-level validation errors
- Invalid email format → 400 Bad Request

---

## 3. API Details

### `POST /api/identity/v1/auth/login`

**Role:** Public (no JWT required)

#### Request Body

```json
{
  "email": "jane.doe@example.com",
  "password": "SecureP@ss1"
}
```

#### Validation Rules

| Field | Constraint | Error Message |
|-------|-----------|---------------|
| email | `@NotBlank`, `@Email` | "Email is required", "Must be a valid email address" |
| password | `@NotBlank` | "Password is required" |

#### Response — 200 OK (Success)

```json
{
  "timestamp": "2026-07-08T17:43:08.677",
  "status": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhMWIyYzNkNC1lNWY2LTc4OTAtYWJjZC1lZjEyMzQ1Njc4OTAiLCJ1c2VybmFtZSI6ImphbmVkb2UiLCJyb2xlcyI6WyJST0xFX1VTRVIiXSwiaWF0IjoxNzgzNjk0NTg4LCJleHAiOjE3ODM3ODA5ODh9.xxxxx"
  }
}
```

#### JWT Token Payload (decoded)

```json
{
  "sub": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "username": "janedoe",
  "roles": ["ROLE_USER"],
  "iat": 1783694588,
  "exp": 1783780988
}
```

#### Response — 401 Unauthorized (Wrong credentials)

```json
{
  "timestamp": "2026-07-08T17:43:15.831",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/identity/v1/auth/login"
}
```

#### Response — 400 Bad Request (Validation failure)

```json
{
  "timestamp": "2026-07-08T17:43:20.412",
  "status": 400,
  "error": "Bad Request",
  "message": "One or more fields failed validation",
  "path": "/api/identity/v1/auth/login",
  "fieldErrors": [
    { "field": "email", "message": "Email is required" },
    { "field": "password", "message": "Password is required" }
  ]
}
```

---

## 4. Database Changes

### Tables created

No new tables created for US-004. The login feature uses the existing `identity.users` and `identity.roles` tables created in US-003.

### Tables used

| Table | Schema | Purpose |
|-------|--------|---------|
| `users` | `identity` | Lookup user by email, retrieve password hash |
| `roles` | `identity` | Fetch role names for JWT claims |
| `user_roles` | `identity` | Join table for user-role mapping |

### Relationships

- `users` ↔ `roles` via `user_roles` (many-to-many, `FetchType.EAGER`)

---

## 5. Data Inserted

**Seed data required:** No (uses existing seed roles from US-003 migration script)

The Flyway migration `V1__create_identity_tables.sql` already inserts the required roles:

```sql
INSERT INTO roles (id, name) VALUES
    (gen_random_uuid(), 'USER'),
    (gen_random_uuid(), 'ADMIN')
ON CONFLICT (name) DO NOTHING;
```

---

## 6. Postman Testing Guide

### Pre-requisite: Register the test user first

Before testing login, register the user using US-003's register endpoint:

**Method:** POST  
**URL:** `http://localhost:8081/api/identity/v1/auth/register`  
**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane.doe@example.com",
  "username": "janedoe",
  "password": "SecureP@ss1"
}
```

**Expected Response (201 Created):**
```json
{
  "timestamp": "2026-07-08T17:40:00.123",
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

---

### Test 1: Successful Login (US-004-login-success)

**Method:** POST  
**URL:** `http://localhost:8081/api/identity/v1/auth/login`  
**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "jane.doe@example.com",
  "password": "SecureP@ss1"
}
```

**Expected Response (200 OK):**
```json
{
  "timestamp": "2026-07-08T17:43:08.677",
  "status": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

> **Verify:** Decode the JWT token at [jwt.io](https://jwt.io) — the `sub` field should match the userId from registration, `username` should be `janedoe`, and `roles` should contain `ROLE_USER`.

---

### Test 2: Wrong Password (US-004-login-wrong-password)

**Method:** POST  
**URL:** `http://localhost:8081/api/identity/v1/auth/login`  
**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "jane.doe@example.com",
  "password": "WrongPassword123"
}
```

**Expected Response (401 Unauthorized):**
```json
{
  "timestamp": "2026-07-08T17:43:15.831",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/identity/v1/auth/login"
}
```

---

### Test 3: Non-existent Email (US-004-login-nonexistent-email)

**Method:** POST  
**URL:** `http://localhost:8081/api/identity/v1/auth/login`  
**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "nobody@example.com",
  "password": "SomePassword1"
}
```

**Expected Response (401 Unauthorized):**
```json
{
  "timestamp": "2026-07-08T17:43:18.245",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/identity/v1/auth/login"
}
```

---

### Test 4: Validation Failure — Blank Fields (US-004-login-validation-failure)

**Method:** POST  
**URL:** `http://localhost:8081/api/identity/v1/auth/login`  
**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "",
  "password": ""
}
```

**Expected Response (400 Bad Request):**
```json
{
  "timestamp": "2026-07-08T17:43:20.412",
  "status": 400,
  "error": "Bad Request",
  "message": "One or more fields failed validation",
  "path": "/api/identity/v1/auth/login",
  "fieldErrors": [
    { "field": "email", "message": "Email is required" },
    { "field": "password", "message": "Password is required" }
  ]
}
```

---

### Test 5: Validation Failure — Invalid Email Format

**Method:** POST  
**URL:** `http://localhost:8081/api/identity/v1/auth/login`  
**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "invalid-email",
  "password": "SecureP@ss1"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "timestamp": "2026-07-08T17:43:22.134",
  "status": 400,
  "error": "Bad Request",
  "message": "One or more fields failed validation",
  "path": "/api/identity/v1/auth/login",
  "fieldErrors": [
    { "field": "email", "message": "Must be a valid email address" }
  ]
}
```

---

## 7. Test Coverage

### Unit Tests Created

| # | Test Method | Scenario |
|---|------------|----------|
| 1 | `should_returnToken_when_validCredentials` | Valid email + password → returns JWT token |
| 2 | `should_generateTokenWithRoles_when_loginSuccessful` | Token generated with ROLE_USER prefix |
| 3 | `should_throwInvalidCredentialsException_when_emailNotFound` | Non-existent email → InvalidCredentialsException |
| 4 | `should_throwInvalidCredentialsException_when_passwordDoesNotMatch` | Wrong password → InvalidCredentialsException |
| 5 | `should_useBcryptMatches_when_verifyingPassword` | BCrypt matches() is called for verification |

### Test File

`identity-service/src/test/java/com/secureclaims/identity/service/impl/AuthServiceImplTest.java`

### Total Tests: 12 (1 integration + 6 registration + 5 login) — ALL PASS ✅

---

## 8. Notes / Assumptions

### Assumptions

- The user must be registered (via US-003) before they can log in.
- JWT secret must be at least 32 characters for HMAC-SHA256 (enforced via `app.jwt.secret` config).
- Roles stored in JWT use the `ROLE_` prefix for Spring Security compatibility (e.g., `ROLE_USER`, `ROLE_ADMIN`).
- The same generic error message is returned for both wrong email and wrong password to prevent user enumeration attacks.

### Limitations

- No account lockout after multiple failed login attempts (can be added in a future story).
- No refresh token mechanism — only a single 24-hour access token is issued.
- No email verification is required before login (user can log in immediately after registration).

### Dependencies

- **Reused from US-003:** `JwtTokenProvider`, `UserRepository`, `PasswordEncoder`, `User` entity, `Role` entity, `ApiResponse` envelope, `GlobalExceptionHandler`
- **New components:** `LoginRequest`, `LoginResponse`, `InvalidCredentialsException`, `login()` method in `AuthService`/`AuthServiceImpl`

---

*Report generated: 2026-07-08 | Story: US-004 | Status: COMPLETE*
