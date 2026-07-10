# Execution Report – US-009: Admin: View and Update Claims

## 1. User Story Summary
- **ID:** US-009
- **Title:** Admin: View and Update Claims
- **Description:** Admins can view all claims in the system and manually update claim status with transition validation.

## 2. Functional Overview
- `GET /admin/claims` lists all claims regardless of owner (ADMIN only)
- `PUT /admin/claims/{id}/status` updates claim status with valid transitions
- Status transitions enforced: SUBMITTED→UNDER_REVIEW→APPROVED/REJECTED→CLOSED
- `ClaimStatusUpdatedEvent` published after successful status update
- Returns 400 for invalid transitions, 403 for USER tokens, 404 for missing claims

## 3. API Details

### GET /api/claims/v1/admin/claims
- **URL:** `http://localhost:8082/api/claims/v1/admin/claims?page=0&size=10`
- **Headers:** `Authorization: Bearer <ADMIN_JWT>`
- **Response (200):** Paginated list of all claims

### PUT /api/claims/v1/admin/claims/{id}/status
- **URL:** `http://localhost:8082/api/claims/v1/admin/claims/{claimId}/status`
- **Headers:** `Authorization: Bearer <ADMIN_JWT>`, `Content-Type: application/json`

**Request Body:**
```json
{
  "status": "UNDER_REVIEW"
}
```

**Success Response (200):**
```json
{
  "timestamp": "2026-07-10T12:15:00",
  "status": 200,
  "message": "Claim status updated",
  "data": {
    "claimId": "c1d2e3f4-a5b6-7890-abcd-ef1234567890",
    "status": "UNDER_REVIEW",
    "submittedAt": "2026-07-10T12:00:00"
  }
}
```

**Invalid Transition (400):**
```json
{
  "timestamp": "2026-07-10T12:15:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot transition from SUBMITTED to CLOSED",
  "path": "/api/claims/v1/admin/claims/{id}/status"
}
```

## 4. Database Changes
- Updates `status` and `updated_by` columns in `claims.claims`

## 5. Data Inserted
- No seed data.

## 6. Postman Testing Guide

### Test 1: Admin List All Claims
```json
{
  "method": "GET",
  "url": "http://localhost:8082/api/claims/v1/admin/claims?page=0&size=10",
  "headers": {
    "Authorization": "Bearer {{adminToken}}"
  },
  "expectedStatus": 200
}
```

### Test 2: Update Claim Status (Valid)
```json
{
  "method": "PUT",
  "url": "http://localhost:8082/api/claims/v1/admin/claims/{{claimId}}/status",
  "headers": {
    "Authorization": "Bearer {{adminToken}}",
    "Content-Type": "application/json"
  },
  "body": {
    "status": "UNDER_REVIEW"
  },
  "expectedStatus": 200
}
```

### Test 3: Invalid Status Transition (400)
```json
{
  "method": "PUT",
  "url": "http://localhost:8082/api/claims/v1/admin/claims/{{claimId}}/status",
  "headers": {
    "Authorization": "Bearer {{adminToken}}",
    "Content-Type": "application/json"
  },
  "body": {
    "status": "CLOSED"
  },
  "expectedStatus": 400
}
```

### Test 4: USER Token Blocked (403)
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
- `ClaimServiceImplTest.should_updateStatus_when_validTransition()`
- `ClaimServiceImplTest.should_throwInvalidTransition_when_invalidStatusChange()`
- `ClaimServiceImplTest.should_throwInvalidTransition_when_invalidStatusValue()`

## 8. Notes / Assumptions
- Valid transitions: SUBMITTED→UNDER_REVIEW, UNDER_REVIEW→APPROVED/REJECTED, APPROVED/REJECTED→CLOSED
- updatedBy set to "ADMIN" for manual updates, "FRAUD_ENGINE" for automatic
