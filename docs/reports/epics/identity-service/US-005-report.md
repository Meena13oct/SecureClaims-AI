# Execution Report: US-005 – JWT Authentication Filter & Security Config

**Epic:** Epic 2 – Identity Service  
**Sprint:** Sprint 1 | **Day:** 3  
**Status:** ✅ Complete  
**Date:** 2026-07-09

---

## 1. User Story Summary

| Field | Value |
|-------|-------|
| Story ID | US-005 |
| Title | JWT Authentication Filter & Security Config |
| Actor | Developer (infrastructure story) |
| Goal | Protect all endpoints except public auth routes with JWT validation |

**Description:** A Spring Security filter that intercepts every HTTP request, extracts and validates the JWT Bearer token from the Authorization header, and sets the Spring Security authentication context. Requests without a valid token to secured endpoints receive a 401 Unauthorized response.

---

## 2. Functional Overview

### What the feature does
- Intercepts all incoming HTTP requests via `OncePerRequestFilter`
- Extracts `Bearer <token>` from the `Authorization` header
- Validates the JWT signature, expiry, and structure
- Builds a `UsernamePasswordAuthenticationToken` with extracted roles as authorities
- Sets the authentication in `SecurityContextHolder` for downstream access
- Returns structured 401 JSON for invalid/expired tokens

### Key business logic implemented
- Bearer token extraction (ignores non-Bearer auth headers)
- JWT validation via JwtTokenProvider
- Role-to-Authority mapping (`ROLE_USER` → `SimpleGrantedAuthority`)
- Stateless session management (`SessionCreationPolicy.STATELESS`)
- Public endpoint whitelisting (`/auth/register`, `/auth/login`, `/actuator/health`, Swagger UI)
- Admin route protection (`/admin/**` requires `ROLE_ADMIN`)

### Edge cases handled
- No Authorization header → filter passes through (Spring Security handles 401)
- Non-Bearer auth header (e.g., Basic) → filter passes through
- Invalid/malformed JWT → returns 401 with JSON error body
- Expired JWT → returns 401 with JSON error body
- Valid token with multiple roles → all roles set as authorities

---

## 3. API Details

> US-005 is an infrastructure story — no new REST endpoints are exposed.

### Security Configuration

| Route Pattern | Access |
|---------------|--------|
| `/api/identity/v1/auth/register` | Public (permitAll) |
| `/api/identity/v1/auth/login` | Public (permitAll) |
| `/actuator/health` | Public (permitAll) |
| `/swagger-ui/**`, `/v3/api-docs/**` | Public (permitAll) |
| `/api/identity/v1/admin/**` | ADMIN role required |
| All other endpoints | Authenticated (valid JWT required) |

### 401 Unauthorized Response (Invalid Token)

```json
{
  "timestamp": "2026-07-09T14:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired JWT token",
  "path": "/api/identity/v1/auth/me"
}
```

---

## 4. Database Changes

No database changes. This is a security filter configuration story.

---

## 5. Data Inserted

No. This story does not require seed data.

---

## 6. Postman Testing Guide

### Test Case 1: Access Secured Endpoint Without Token (401)

**Method:** GET  
**URL:** `http://13.207.68.121:8081/api/identity/v1/auth/me`

**Headers:**
```
(no Authorization header)
```

**Expected Response (401 Unauthorized):**
```json
{
  "timestamp": "2026-07-09T14:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired JWT token",
  "path": "/api/identity/v1/auth/me"
}
```

---

### Test Case 2: Access Secured Endpoint With Invalid Token (401)

**Method:** GET  
**URL:** `http://13.207.68.121:8081/api/identity/v1/auth/me`

**Headers:**
```
Authorization: Bearer invalid.token.string
```

**Expected Response (401 Unauthorized):**
```json
{
  "timestamp": "2026-07-09T14:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired JWT token",
  "path": "/api/identity/v1/auth/me"
}
```

---

### Test Case 3: Access Public Endpoint Without Token (200)

**Method:** GET  
**URL:** `http://13.207.68.121:8081/actuator/health`

**Headers:**
```
(no Authorization header)
```

**Expected Response (200 OK):**
```json
{
  "status": "UP"
}
```

---

### Test Case 4: Access Secured Endpoint With Valid Token (200)

**Pre-requisite:** Login first to get a token.

**Step 1 — Login:**

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

**Step 2 — Use Token:**

**Method:** GET  
**URL:** `http://13.207.68.121:8081/api/identity/v1/auth/me`

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response (200 OK):**
```json
{
  "timestamp": "2026-07-09T14:00:00",
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

---

## 7. Test Coverage

### Unit Tests: 5 tests in `JwtAuthenticationFilterTest`

| # | Test Method | Scenario |
|---|-------------|----------|
| 1 | `should_continueFilterChain_when_noAuthorizationHeader` | No header → passes through, no auth set |
| 2 | `should_continueFilterChain_when_authHeaderNotBearer` | Basic auth header → passes through |
| 3 | `should_return401_when_tokenIsInvalid` | Invalid token → 401 response, chain not continued |
| 4 | `should_setAuthentication_when_tokenIsValid` | Valid token → auth context set with correct principal/authorities |
| 5 | `should_setMultipleAuthorities_when_tokenHasMultipleRoles` | Multi-role token → both authorities present |

**Result:** All 5 tests pass ✅

---

## 8. Notes / Assumptions

- The filter does NOT throw exceptions — it writes the 401 response directly for invalid tokens
- For missing tokens on secured endpoints, Spring Security's default `AuthenticationEntryPoint` handles the 401 (filter just passes through)
- `@EnableMethodSecurity` is enabled for `@PreAuthorize` support in controllers
- The filter is registered BEFORE `UsernamePasswordAuthenticationFilter` in the chain
- Session creation is disabled (`STATELESS`) — no server-side sessions
- CSRF is disabled (appropriate for stateless JWT APIs)

---

*Report generated: 2026-07-09 | US-005 Implementation Complete*
