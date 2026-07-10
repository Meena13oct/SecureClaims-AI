# Execution Report – US-007: Submit a Claim

## 1. User Story Summary
- **ID:** US-007
- **Title:** Submit a Claim
- **Description:** Authenticated users can submit insurance claims with policy details. Claims are recorded and automatically evaluated for fraud via event publishing.

## 2. Functional Overview
- Users submit claims via `POST /api/claims/v1/claims` with a valid JWT
- Claim saved with auto-generated UUID, status=SUBMITTED, userId from JWT
- `ClaimCreatedEvent` published to in-memory event bus after successful save
- Bean validation enforced on all required fields
- Edge cases: missing fields → 400, invalid JWT → 401

## 3. API Details

### POST /api/claims/v1/claims
- **Method:** POST
- **URL:** `http://localhost:8082/api/claims/v1/claims`
- **Headers:** `Authorization: Bearer <JWT>`, `Content-Type: application/json`

**Request Body:**
```json
{
  "policyNumber": "POL-2026-00123",
  "claimType": "MEDICAL",
  "incidentDate": "2026-06-20",
  "description": "Hospitalisation due to surgery",
  "claimedAmount": 75000.00,
  "policyAgeMonths": 12
}
```

**Success Response (201 Created):**
```json
{
  "timestamp": "2026-07-10T12:00:00",
  "status": 201,
  "message": "Claim submitted successfully",
  "data": {
    "claimId": "c1d2e3f4-a5b6-7890-abcd-ef1234567890",
    "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "policyNumber": "POL-2026-00123",
    "claimType": "MEDICAL",
    "incidentDate": "2026-06-20",
    "description": "Hospitalisation due to surgery",
    "claimedAmount": 75000.00,
    "status": "SUBMITTED",
    "submittedAt": "2026-07-10T12:00:00"
  }
}
```

**Validation Error (400):**
```json
{
  "timestamp": "2026-07-10T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "One or more fields failed validation",
  "path": "/api/claims/v1/claims",
  "fieldErrors": [
    {"field": "policyNumber", "message": "Policy number is required"}
  ]
}
```

## 4. Database Changes
- **Table:** `claims.claims`
- **Columns:** id (UUID PK), user_id, policy_number, claim_type, incident_date, description, claimed_amount, status, policy_age_months, updated_by, submitted_at, updated_at

## 5. Data Inserted
- No seed data. Claims are created via API at runtime.

## 6. Postman Testing Guide

### Test 1: Submit Claim Successfully
```json
{
  "method": "POST",
  "url": "http://localhost:8082/api/claims/v1/claims",
  "headers": {
    "Authorization": "Bearer {{token}}",
    "Content-Type": "application/json"
  },
  "body": {
    "policyNumber": "POL-2026-00123",
    "claimType": "MEDICAL",
    "incidentDate": "2026-06-20",
    "description": "Hospitalisation due to surgery",
    "claimedAmount": 75000.00,
    "policyAgeMonths": 12
  },
  "expectedStatus": 201
}
```

### Test 2: Submit Claim with Missing Fields (Validation)
```json
{
  "method": "POST",
  "url": "http://localhost:8082/api/claims/v1/claims",
  "headers": {
    "Authorization": "Bearer {{token}}",
    "Content-Type": "application/json"
  },
  "body": {
    "claimType": "MEDICAL"
  },
  "expectedStatus": 400
}
```

## 7. Test Coverage
- `ClaimServiceImplTest.should_createClaim_when_validRequest()` — verifies claim creation
- `ClaimServiceImplTest.should_publishClaimCreatedEvent_when_claimCreated()` — verifies event publishing

## 8. Notes / Assumptions
- `policyAgeMonths` defaults to 12 if not provided
- ClaimCreatedEvent is consumed by FraudEventHandler for automatic fraud scoring
