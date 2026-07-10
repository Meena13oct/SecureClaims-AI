# Postman Test Data - Quick Reference

**Production Base URL:** `http://13.207.68.121`
**Local Base URL:** `http://localhost`

---

## Step 1: Register a User (if not already done)

**POST** `http://13.207.68.121:8081/api/identity/v1/auth/register`

Headers: `Content-Type: application/json`

```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane.doe@example.com",
  "username": "janedoe",
  "password": "SecureP@ss1"
}
```

---

## Step 2: Login to Get JWT Token

**POST** `http://13.207.68.121:8081/api/identity/v1/auth/login`

Headers: `Content-Type: application/json`

```json
{
  "email": "jane.doe@example.com",
  "password": "SecureP@ss1"
}
```

**Response:** Copy the `token` field from `data.token`

---

## Step 3: Use Token for All Secured Endpoints

Add this header to all requests:
```
Authorization: Bearer <paste-your-token-here>
```

---

## US-005: GET /auth/me (Profile from JWT)

**GET** `http://13.207.68.121:8081/api/identity/v1/auth/me`

Headers:
```
Authorization: Bearer <your-token>
```

Expected Response:
```json
{
  "timestamp": "2026-07-10T12:00:00",
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

## US-007: Submit a Claim

**POST** `http://13.207.68.121:8082/api/claims/v1/claims`

Headers:
```
Authorization: Bearer <your-token>
Content-Type: application/json
```

```json
{
  "policyNumber": "POL-2026-00123",
  "claimType": "MEDICAL",
  "incidentDate": "2026-06-20",
  "description": "Hospitalisation due to surgery",
  "claimedAmount": 75000.00,
  "policyAgeMonths": 3
}
```

---

## US-008: View My Claims

**GET** `http://13.207.68.121:8082/api/claims/v1/claims?page=0&size=10`

Headers:
```
Authorization: Bearer <your-token>
```

---

## US-008: Get Specific Claim

**GET** `http://13.207.68.121:8082/api/claims/v1/claims/{claimId}`

Headers:
```
Authorization: Bearer <your-token>
```

---

## US-009: Admin - List All Claims

**GET** `http://13.207.68.121:8082/api/claims/v1/admin/claims?page=0&size=10`

Headers:
```
Authorization: Bearer <admin-token>
```

---

## US-009: Admin - Update Claim Status

**PUT** `http://13.207.68.121:8082/api/claims/v1/admin/claims/{claimId}/status`

Headers:
```
Authorization: Bearer <admin-token>
Content-Type: application/json
```

```json
{
  "status": "APPROVED"
}
```

Valid transitions: `SUBMITTED → UNDER_REVIEW → APPROVED/REJECTED → CLOSED`

---

## US-010: Upload Document (PDF only)

**POST** `http://13.207.68.121:8082/api/claims/v1/claims/{claimId}/documents`

Headers:
```
Authorization: Bearer <your-token>
Content-Type: multipart/form-data
```

Body: form-data with key `file`, select a PDF file (max 10MB)

---

## US-010: List Documents

**GET** `http://13.207.68.121:8082/api/claims/v1/claims/{claimId}/documents`

Headers:
```
Authorization: Bearer <your-token>
```

---

## US-015: Admin - Get Fraud Analysis

**GET** `http://13.207.68.121:8083/api/fraud/v1/admin/fraud/{claimId}`

Headers:
```
Authorization: Bearer <admin-token>
```

---

## US-015: Admin - Get Notification History

**GET** `http://13.207.68.121:8084/api/notifications/v1/admin/notifications/{userId}?page=0&size=10`

Headers:
```
Authorization: Bearer <admin-token>
```

---

## US-016: Admin - List All Users

**GET** `http://13.207.68.121:8081/api/identity/v1/admin/users?page=0&size=10`

Headers:
```
Authorization: Bearer <admin-token>
```

---

## Admin Login Credentials

```json
{
  "email": "admin@secureclaims.com",
  "password": "AdminP@ss1"
}
```

> Note: Admin user must be registered first AND have ADMIN role assigned in the database.
> Use SQL: `INSERT INTO identity.user_roles (user_id, role_id) SELECT u.id, r.id FROM identity.users u, identity.roles r WHERE u.email = 'admin@secureclaims.com' AND r.name = 'ADMIN';`

---

## Test Data Relationships

| User | Email | Role | Purpose |
|------|-------|------|---------|
| Jane Doe | jane.doe@example.com | USER | Submit/view claims |
| Admin User | admin@secureclaims.com | ADMIN | Manage claims, view fraud/notifications |

| Claim | Amount | Policy Age | Expected Risk | Expected Status |
|-------|--------|------------|--------------|-----------------|
| Low Risk | $5,000 | 24 months | LOW (0 pts) | UNDER_REVIEW |
| Medium Risk | $25,000 | 3 months | MEDIUM (3 pts) | UNDER_REVIEW |
| High Risk | $60,000 | 3 months | HIGH (5 pts) | REJECTED |
