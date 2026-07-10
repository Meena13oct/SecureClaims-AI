# Execution Report – US-017: Global Exception Handler

## 1. User Story Summary
- **ID:** US-017
- **Title:** Global Exception Handler
- **Description:** `GlobalExceptionHandler` in every service returns consistent structured JSON error responses.

## 2. Functional Overview
- `@RestControllerAdvice` class present in all four services
- Handles: MethodArgumentNotValidException (400), ResourceNotFoundException (404), AccessDeniedException (403), generic Exception (500)
- Consistent error response shape: `{ timestamp, status, error, message, path }`
- Validation errors include `fieldErrors` array with field-level messages
- No stack traces exposed in response bodies

## 3. API Details
- **No dedicated endpoints** — exception handling is cross-cutting

**Standard Error Response Shape:**
```json
{
  "timestamp": "2026-07-10T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "One or more fields failed validation",
  "path": "/api/claims/v1/claims",
  "fieldErrors": [
    {"field": "policyNumber", "message": "Policy number is required"},
    {"field": "claimedAmount", "message": "Claimed amount is required"}
  ]
}
```

**404 Not Found:**
```json
{
  "timestamp": "2026-07-10T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Claim not found with id: 'abc-123'",
  "path": "/api/claims/v1/claims/abc-123"
}
```

**403 Forbidden:**
```json
{
  "timestamp": "2026-07-10T12:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied",
  "path": "/api/claims/v1/admin/claims"
}
```

**500 Internal Server Error:**
```json
{
  "timestamp": "2026-07-10T12:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "path": "/api/claims/v1/claims"
}
```

## 4. Database Changes
- None. Exception handling is application-layer configuration.

## 5. Data Inserted
- None.

## 6. Postman Testing Guide

### Test 1: Trigger Validation Error (400)
```json
{
  "method": "POST",
  "url": "http://localhost:8082/api/claims/v1/claims",
  "headers": {
    "Authorization": "Bearer {{token}}",
    "Content-Type": "application/json"
  },
  "body": {},
  "expectedStatus": 400,
  "expectedResponseFields": ["timestamp", "status", "error", "message", "path", "fieldErrors"]
}
```

### Test 2: Trigger Not Found (404)
```json
{
  "method": "GET",
  "url": "http://localhost:8082/api/claims/v1/claims/00000000-0000-0000-0000-000000000000",
  "headers": {
    "Authorization": "Bearer {{token}}"
  },
  "expectedStatus": 404,
  "expectedResponseFields": ["timestamp", "status", "error", "message", "path"]
}
```

### Test 3: Trigger Forbidden (403) — USER on Admin endpoint
```json
{
  "method": "GET",
  "url": "http://localhost:8082/api/claims/v1/admin/claims",
  "headers": {
    "Authorization": "Bearer {{userToken}}"
  },
  "expectedStatus": 403
}
```

## 7. Test Coverage
- All error scenarios tested implicitly through ClaimServiceImplTest and AuthServiceImplTest
- ResourceNotFoundException, InvalidStatusTransitionException, AccessDeniedException all verified

## 8. Notes / Assumptions
- All four services (identity, claims, fraud, notification) have consistent handler
- Claims service has additional handlers for `InvalidStatusTransitionException` and `IllegalArgumentException`
- Identity service has additional handlers for `DuplicateResourceException` and `InvalidCredentialsException`
