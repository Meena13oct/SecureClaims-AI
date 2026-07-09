# Execution Report: US-006 – Role-Based Access Control & User Profile

**Epic:** Epic 2 – Identity Service  
**Sprint:** Sprint 1 | **Day:** 3  
**Status:** ✅ Complete  
**Date:** 2026-07-09

---

## 1. User Story Summary

| Field | Value |
|-------|-------|
| Story ID | US-006 |
| Title | Role-Based Access Control & User Profile |
| Actor | Developer / Authenticated User |
| Goal | Enforce USER and ADMIN roles on endpoints and provide a current-user profile endpoint |

**Description:** Implements role-based access control where USER role grants access to general endpoints and ADMIN role is required for `/admin/**` routes. A `GET /auth/me` endpoint returns the current user's profile entirely from JWT claims without a database call.

---

## 2. Functional Overview

### What the feature does
- `GET /api/identity/v1/auth/me` returns user profile resolved from JWT (no DB call)
- Extracts userId, username, email, firstName, lastName, and roles from JWT claims
- `/admin/**` endpoints enforce `hasRole('ADMIN')` via SecurityConfig
- `@EnableMethodSecurity` enables `@PreAuthorize` annotations on controllers
- JWT now includes email, firstName, and lastName as additional claims

### Key business logic implemented
- Full user profile embedded in JWT at login time (avoids DB calls on `/auth/me`)
- Role enforcement at two levels:
  1. `SecurityConfig.requestMatchers("/admin/**").hasRole("ADMIN")` — URL-level
  2. `@PreAuthorize("hasRole('ADMIN')")` — method-level (for future admin controllers)
- JWT claims enrichment: added email, firstName, lastName to token generation
- Backward-compatible `generateToken` overload (3-param version still works)

### Edge cases handled
- Missing JWT on `/auth/me` → 401 (handled by JwtAuthenticationFilter from US-005)
- USER token on `/admin/**` → 403 Forbidden (Spring Security denies)
- Invalid token on `/auth/me` → 401 (handled by filter)
- Null email/firstName/lastName in backward-compatible tokens → returns null in response

---

## 3. API Details

### `GET /api/identity/v1/auth/me`

**Role:** USER or ADMIN (any valid JWT)  
**Description:** Returns the authenticated user's profile, resolved entirely from JWT claims.

#### Headers

```
Authorization: Bearer <JWT>
```

#### Response: 200 OK

```json
{
  "timestamp": "2026-07-09T14:10:00",
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

#### Response: 401 Unauthorized (No/Invalid Token)

```json
{
  "timestamp": "2026-07-09T14:10:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired JWT token",
  "path": "/api/identity/v1/auth/me"
}
```

### Admin Route Protection

| Route | Required Role | Response for USER token |
|-------|--------------|------------------------|
| `/api/identity/v1/admin/**` | ADMIN | 403 Forbidden |

---

## 4. Database Changes

No database changes. The `/auth/me` endpoint resolves all data from JWT claims. The JWT was enriched with additional claims (email, firstName, lastName) at token generation time during login.

---

## 5. Data Inserted

No. This story uses existing user data created during registration (US-003).

---

## 6. Postman Testing Guide

### Test Case 1: Login to Get Token

**Method:** POST  
**URL:** `http://13.207.68.121:8081/api/identity/v1/auth/login`

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
  "timestamp": "2026-07-09T14:10:00",
  "status": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

### Test Case 2: Get Current User Profile (200)

**Method:** GET  
**URL:** `http://13.207.68.121:8081/api/identity/v1/auth/me`

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response (200 OK):**
```json
{
  "timestamp": "2026-07-09T14:10:00",
  "status": 200,
  "message": "Request processed successfully",
  "data": {
    "userId": "537dcece-ecb1-4ca6-8c20-212f13b472ca",
    "username": "janedoe",
    "email": "jane.doe@example.com",
    "firstName": "Jane",
    "lastName": "Doe",
    "roles": ["ROLE_USER"]
  }
}
```

---

### Test Case 3: Get Profile Without Token (401)

**Method:** GET  
**URL:** `http://13.207.68.121:8081/api/identity/v1/auth/me`

**Headers:**
```
(no Authorization header)
```

**Expected Response (401 Unauthorized):**
```json
{
  "timestamp": "2026-07-09T14:10:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired JWT token",
  "path": "/api/identity/v1/auth/me"
}
```

---

### Test Case 4: Access Admin Endpoint with USER Token (403)

**Method:** GET  
**URL:** `http://13.207.68.121:8081/api/identity/v1/admin/users`

**Headers:**
```
Authorization: Bearer {{userToken}}
```

**Expected Response (403 Forbidden):**
```json
{
  "timestamp": "2026-07-09T14:10:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied",
  "path": "/api/identity/v1/admin/users"
}
```

---

## 7. Test Coverage

### Unit Tests: 10 tests in `JwtTokenProviderTest`

| # | Test Method | Scenario |
|---|-------------|----------|
| 1 | `should_generateValidToken_when_allFieldsProvided` | Token generation with 6 fields works |
| 2 | `should_extractUserId_when_tokenIsValid` | Subject claim extracted correctly |
| 3 | `should_extractUsername_when_tokenIsValid` | Username claim extracted |
| 4 | `should_extractEmail_when_tokenIsValid` | Email claim extracted |
| 5 | `should_extractFirstName_when_tokenIsValid` | First name claim extracted |
| 6 | `should_extractLastName_when_tokenIsValid` | Last name claim extracted |
| 7 | `should_extractRoles_when_tokenIsValid` | Roles list extracted |
| 8 | `should_returnFalse_when_tokenIsInvalid` | Invalid token rejected |
| 9 | `should_returnFalse_when_tokenIsExpired` | Expired token rejected |
| 10 | `should_generateToken_when_usingBackwardCompatibleOverload` | 3-param overload still works |

### Updated Tests: 11 tests in `AuthServiceImplTest` (login tests updated for new signature)

**Total test count for Epic 2 (US-003 through US-006): 27 tests, all passing ✅**

---

## 8. Notes / Assumptions

- JWT now contains 6 claims: `sub` (userId), `username`, `email`, `firstName`, `lastName`, `roles`
- This makes the token slightly larger but eliminates DB calls for `/auth/me`
- The backward-compatible 3-param `generateToken` sets email/firstName/lastName to null
- Roles in JWT use Spring Security format: `ROLE_USER`, `ROLE_ADMIN`
- Admin endpoints don't exist yet — they'll be created in US-016. Currently, any request to `/admin/**` with USER token gets 403
- `@EnableMethodSecurity` allows future controllers to use `@PreAuthorize("hasRole('ADMIN')")` at method level

---

*Report generated: 2026-07-09 | US-006 Implementation Complete*
