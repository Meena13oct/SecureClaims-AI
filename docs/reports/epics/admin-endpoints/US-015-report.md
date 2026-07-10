# Execution Report – US-015: Admin: View Fraud Analysis and Notification History

## 1. User Story Summary
- **ID:** US-015
- **Title:** Admin: View Fraud Analysis and Notification History
- **Description:** Admin endpoints to retrieve fraud analysis results for a claim and notification history for a user.

## 2. Functional Overview
- `GET /admin/fraud/{claimId}` returns fraud analysis: riskScore, riskLevel, analysisNotes, analyzedAt
- `GET /admin/notifications/{userId}` returns paginated notification history
- Both endpoints require ADMIN JWT — returns 403 for USER tokens
- Returns 404 if no fraud analysis exists for the given claimId

## 3. API Details

### GET /api/fraud/v1/admin/fraud/{claimId}
- **URL:** `http://localhost:8083/api/fraud/v1/admin/fraud/{claimId}`
- **Headers:** `Authorization: Bearer <ADMIN_JWT>`

**Success Response (200):**
```json
{
  "timestamp": "2026-07-10T12:00:00",
  "status": 200,
  "message": "Fraud analysis retrieved",
  "data": {
    "claimId": "c1d2e3f4-a5b6-7890-abcd-ef1234567890",
    "riskScore": 5,
    "riskLevel": "HIGH",
    "analysisNotes": "Amount rule: +3 (claimed $60000 > $50,000); Policy age rule: +2 (3 months < 6 months); Prior claims rule: +0; Total: 5 → HIGH",
    "analyzedAt": "2026-07-10T12:01:00"
  }
}
```

### GET /api/notifications/v1/admin/notifications/{userId}
- **URL:** `http://localhost:8084/api/notifications/v1/admin/notifications/{userId}?page=0&size=10`
- **Headers:** `Authorization: Bearer <ADMIN_JWT>`

**Success Response (200):**
```json
{
  "timestamp": "2026-07-10T12:05:00",
  "status": 200,
  "message": "Notifications retrieved",
  "data": {
    "content": [
      {
        "notificationId": "n1o2p3q4-r5s6-7890-abcd-ef1234567890",
        "type": "CLAIM_SUBMITTED",
        "channel": "EMAIL",
        "message": "Your claim c1d2e3f4... has been submitted successfully.",
        "sentAt": "2026-07-10T12:00:30"
      }
    ],
    "pageable": {"pageNumber": 0, "pageSize": 10},
    "totalElements": 4,
    "totalPages": 1
  }
}
```

## 4. Database Changes
- No new tables. Reads from `fraud.fraud_analyses` and `notifications.notifications`.

## 5. Data Inserted
- No seed data.

## 6. Postman Testing Guide

### Test 1: Get Fraud Analysis (Admin)
```json
{
  "method": "GET",
  "url": "http://localhost:8083/api/fraud/v1/admin/fraud/{{claimId}}",
  "headers": {
    "Authorization": "Bearer {{adminToken}}"
  },
  "expectedStatus": 200
}
```

### Test 2: Get Notification History (Admin)
```json
{
  "method": "GET",
  "url": "http://localhost:8084/api/notifications/v1/admin/notifications/{{userId}}?page=0&size=10",
  "headers": {
    "Authorization": "Bearer {{adminToken}}"
  },
  "expectedStatus": 200
}
```

### Test 3: USER Token Blocked (403)
```json
{
  "method": "GET",
  "url": "http://localhost:8083/api/fraud/v1/admin/fraud/{{claimId}}",
  "headers": {
    "Authorization": "Bearer {{userToken}}"
  },
  "expectedStatus": 403
}
```

## 7. Test Coverage
- Service layer tests verify query operations
- Admin access control verified via SecurityConfig with `hasRole('ADMIN')`

## 8. Notes / Assumptions
- FraudAdminController in fraud-detection-service handles `/admin/fraud/{claimId}`
- NotificationAdminController in notification-service handles `/admin/notifications/{userId}`
