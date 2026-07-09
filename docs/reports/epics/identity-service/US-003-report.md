# Execution Report: US-003 – User Registration

**Epic:** Epic 2 – Identity Service  
**Sprint:** Sprint 1 | **Day:** 2  
**Status:** ✅ Complete  
**Date:** 2026-07-09

---

## 1. User Story Summary

| Field | Value |
|-------|-------|
| Story ID | US-003 |
| Title | User Registration |
| Actor | Guest (unauthenticated user) |
| Goal | Register an account with name, email, username, and password to access the system |

**Description:** A guest can create an account by providing their personal details. The system validates input, checks for duplicates, hashes the password with BCrypt, assigns the USER role, and returns the created user details without exposing the password.

---

## 2. Functional Overview

### What the feature does
- Accepts user registration requests via a public REST endpoint
- Validates all input fields using Bean Validation annotations
- Checks for duplicate email and username in the database
- Hashes the password using BCrypt before storage
- Assigns the default USER role to the new account
- Returns user details (excluding password) with a 201 Created status

### Key business logic implemented
- BCrypt password hashing (never stores plain text)
- Duplicate email detection → 409 Conflict
- Duplicate username detection → 409 Conflict
- Automatic USER role assignment from roles table
- Field-level validation with descriptive error messages

### Edge cases handled
- Missing required fields → 400 Bad Request with field-level errors
- Invalid email format → 400 Bad Request
- Password too short (< 8 chars) → 400 Bad Request
- Email already registered → 409 Conflict
- Username already taken → 409 Conflict
- USER role not found in database → 404 Not Found (system integrity issue)

---

## 3. API Details

### `POST /api/identity/v1/auth/register`

**Role:** Public (no JWT required)

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

| Field | Constraint | Error Message |
|-------|-----------|---------------|
| firstName | `@NotBlank` | "First name is required" |
| lastName | `@NotBlank` | "Last name is required" |
| email | `@NotBlank`, `@Email` | "Email is required" / "Must be a valid email address" |
| username | `@NotBlank` | "Username is required" |
| password | `@NotBlank`, `@Size(min=8)` | "Password is required" / "Password must be at least 8 characters" |

#### Response: 201 Created

```json
{
  "timestamp": "2026-07-09T10:00:00",
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

#### Response: 400 Bad Request (Validation Failure)

```json
{
  "timestamp": "2026-07-09T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "One or more fields failed validation",
  "path": "/api/identity/v1/auth/register",
  "fieldErrors": [
    { "field": "email", "message": "Must be a valid email address" },
    { "field": "password", "message": "Password must be at least 8 characters" }
  ]
}
```

#### Response: 409 Conflict (Duplicate)

```json
{
  "timestamp": "2026-07-09T10:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Email already in use",
  "path": "/api/identity/v1/auth/register"
}
```

---

## 4. Database Changes

### Schema: `identity`

### Tables Created

#### `identity.users`

| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() |
| username | VARCHAR(50) | NOT NULL, UNIQUE |
| email | VARCHAR(100) | NOT NULL, UNIQUE |
| first_name | VARCHAR(50) | NOT NULL |
| last_name | VARCHAR(50) | NOT NULL |
| password_hash | VARCHAR(255) | NOT NULL |
| is_active | BOOLEAN | NOT NULL, DEFAULT TRUE |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

**Indexes:**
- `idx_users_email` on `email`
- `idx_users_username` on `username`

#### `identity.roles`

| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() |
| name | VARCHAR(20) | NOT NULL, UNIQUE |

#### `identity.user_roles` (Join Table)

| Column | Type | Constraints |
|--------|------|-------------|
| user_id | UUID | NOT NULL, FK → users(id) ON DELETE CASCADE |
| role_id | UUID | NOT NULL, FK → roles(id) ON DELETE CASCADE |

**Primary Key:** (user_id, role_id)  
**Index:** `idx_user_roles_user_id` on `user_id`

### Relationships
- `users` ↔ `roles`: Many-to-Many through `user_roles`

---

## 5. Data Inserted

**Yes** – Seed data inserted via Flyway migration `V1__create_identity_tables.sql`

### Table: `identity.roles`

```sql
INSERT INTO roles (id, name) VALUES
    (gen_random_uuid(), 'USER'),
    (gen_random_uuid(), 'ADMIN')
ON CONFLICT (name) DO NOTHING;
```

| id | name |
|----|------|
| (auto-generated UUID) | USER |
| (auto-generated UUID) | ADMIN |

---

## 6. Postman Testing Guide

### Test Case 1: Register New User (Success - 201)

**Method:** POST  
**URL:** `http://13.207.68.121:8081/api/identity/v1/auth/register`

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
  "timestamp": "2026-07-09T10:00:00",
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

**Test Script:**
```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});
pm.test("Response has user data", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.data.userId).to.not.be.undefined;
    pm.expect(jsonData.data.username).to.eql("janedoe");
    pm.expect(jsonData.data.email).to.eql("jane.doe@example.com");
    pm.expect(jsonData.data.roles).to.include("USER");
});
pm.test("Password not in response", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.data.password).to.be.undefined;
    pm.expect(jsonData.data.passwordHash).to.be.undefined;
});
```

---

### Test Case 2: Register with Missing Fields (Validation - 400)

**Method:** POST  
**URL:** `http://13.207.68.121:8081/api/identity/v1/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "firstName": "",
  "lastName": "",
  "email": "invalid-email",
  "username": "",
  "password": "short"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "timestamp": "2026-07-09T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "One or more fields failed validation",
  "path": "/api/identity/v1/auth/register",
  "fieldErrors": [
    { "field": "firstName", "message": "First name is required" },
    { "field": "lastName", "message": "Last name is required" },
    { "field": "email", "message": "Must be a valid email address" },
    { "field": "username", "message": "Username is required" },
    { "field": "password", "message": "Password must be at least 8 characters" }
  ]
}
```

**Test Script:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});
pm.test("Response has field errors", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.fieldErrors).to.be.an("array");
    pm.expect(jsonData.fieldErrors.length).to.be.greaterThan(0);
});
pm.test("Error message is correct", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.message).to.eql("One or more fields failed validation");
});
```

---

### Test Case 3: Register with Duplicate Email (Conflict - 409)

**Method:** POST  
**URL:** `http://13.207.68.121:8081/api/identity/v1/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Request Body (use same email as Test Case 1):**
```json
{
  "firstName": "John",
  "lastName": "Smith",
  "email": "jane.doe@example.com",
  "username": "johnsmith",
  "password": "AnotherP@ss1"
}
```

**Expected Response (409 Conflict):**
```json
{
  "timestamp": "2026-07-09T10:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Email already in use",
  "path": "/api/identity/v1/auth/register"
}
```

**Test Script:**
```javascript
pm.test("Status code is 409", function () {
    pm.response.to.have.status(409);
});
pm.test("Conflict message is correct", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.message).to.eql("Email already in use");
});
pm.test("Error type is Conflict", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.error).to.eql("Conflict");
});
```

---

## 7. Test Coverage

### Unit Tests Created: 11 tests in `AuthServiceImplTest`

| # | Test Method | Scenario |
|---|-------------|----------|
| 1 | `should_registerUser_when_validRequest` | Successful registration returns correct UserResponse |
| 2 | `should_hashPassword_when_registeringUser` | Password is BCrypt hashed before saving |
| 3 | `should_throwDuplicateResourceException_when_emailAlreadyExists` | Duplicate email → DuplicateResourceException |
| 4 | `should_throwDuplicateResourceException_when_usernameAlreadyExists` | Duplicate username → DuplicateResourceException |
| 5 | `should_throwResourceNotFoundException_when_userRoleNotFound` | Missing USER role → ResourceNotFoundException |
| 6 | `should_assignUserRole_when_registering` | USER role is assigned to new user |
| 7 | `should_returnToken_when_validCredentials` | Valid login returns JWT token |
| 8 | `should_generateTokenWithRoles_when_loginSuccessful` | JWT includes ROLE_USER prefix |
| 9 | `should_throwInvalidCredentialsException_when_emailNotFound` | Unknown email → InvalidCredentialsException |
| 10 | `should_throwInvalidCredentialsException_when_passwordDoesNotMatch` | Wrong password → InvalidCredentialsException |
| 11 | `should_useBcryptMatches_when_verifyingPassword` | BCrypt matches() is used for verification |

**Result:** All 11 tests pass ✅

---

## 8. Notes / Assumptions

- **Flyway** is used for database migrations (`V1__create_identity_tables.sql`)
- The USER and ADMIN roles are seeded automatically on first migration run
- `ddl-auto=update` is also enabled, so Hibernate will create/update tables in addition to Flyway
- Password minimum length is 8 characters (no complexity requirements beyond that in this story)
- The `@JsonIgnore` annotation on the `passwordHash` field in the User entity ensures it's never serialized
- The endpoint is public — no JWT token is required for registration
- The response includes the auto-generated UUID as `userId`
- Roles are stored as plain names (`USER`, `ADMIN`) in the database, prefixed with `ROLE_` only in JWT claims

---

*Report generated: 2026-07-09 | US-003 Implementation Complete*
