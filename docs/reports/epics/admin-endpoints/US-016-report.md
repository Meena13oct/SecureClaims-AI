# Execution Report – US-016: Admin: List All Users

## 1. User Story Summary
- **ID:** US-016
- **Title:** Admin: List All Users
- **Description:** Admin endpoint to retrieve a paginated list of all registered users for auditing purposes.

## 2. Functional Overview
- `GET /admin/users` returns all users in the system (ADMIN JWT required)
- Each record includes: userId, username, email, firstName, lastName, roles
- Returns 403 for USER tokens
- Supports pagination via `page` and `size` query parameters
- Implemented in `AdminUserController` + `UserAdminServiceImpl`

## 3. API Details

### GET /api/identity/v1/admin/users
- **URL:** `http://localhost:8081/api/identity/v1/admin/users?page=0&size=10`
- **Headers:** `Authorization: Bearer <ADMIN_JWT>`

**Success Response (200):**
```json
{
  "timestamp": "2026-07-10T12:10:00",
  "status": 200,
  "message": "Users retrieved successfully",
  "data": {
    "content": [
      {
        "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        "username": "janedoe",
        "email": "jane.doe@example.com",
        "firstName": "Jane",
        "lastName": "Doe",
        "roles": ["USER"]
      },
      {
        "userId": "b2c3d4e5-f6a7-8901-bcde-fg2345678901",
        "username": "adminuser",
        "email": "admin@secureclaims.com",
        "firstName": "Admin",
        "lastName": "User",
        "roles": ["ADMIN"]
      }
    ],
    "pageable": {"pageNumber": 0, "pageSize": 10},
    "totalElements": 2,
    "totalPages": 1
  }
}
```

**Forbidden (403) — USER token:**
```json
{
  "timestamp": "2026-07-10T12:10:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied",
  "path": "/api/identity/v1/admin/users"
}
```

## 4. Database Changes
- No new tables. Reads from existing `identity.users` and `identity.user_roles`.

## 5. Data Inserted
- No additional seed data. Uses existing registered users.

## 6. Postman Testing Guide

### Test 1: Admin List All Users (Success)
```json
{
  "method": "GET",
  "url": "http://localhost:8081/api/identity/v1/admin/users?page=0&size=10",
  "headers": {
    "Authorization": "Bearer {{adminToken}}"
  },
  "expectedStatus": 200
}
```

### Test 2: USER Token Blocked (403)
```json
{
  "method": "GET",
  "url": "http://localhost:8081/api/identity/v1/admin/users",
  "headers": {
    "Authorization": "Bearer {{userToken}}"
  },
  "expectedStatus": 403
}
```

### Test 3: No Token (401)
```json
{
  "method": "GET",
  "url": "http://localhost:8081/api/identity/v1/admin/users",
  "headers": {},
  "expectedStatus": 401
}
```

## 7. Test Coverage
- `@PreAuthorize("hasRole('ADMIN')")` enforces role-based access
- SecurityConfig already maps `/api/identity/v1/admin/**` to ADMIN role
- Integration testing verifies 403 response for non-admin tokens

## 8. Notes / Assumptions
- Password hashes never included in response (UserResponse DTO excludes password)
- Roles returned as role names without ROLE_ prefix (e.g., "USER", "ADMIN")
