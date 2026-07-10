# Execution Report – US-008: View Claims

## 1. User Story Summary
- **ID:** US-008
- **Title:** View Claims
- **Description:** Authenticated users can view all their submitted claims and full details of a specific claim, with ownership enforcement.

## 2. Functional Overview
- `GET /claims` returns paginated list of claims belonging to the authenticated user
- `GET /claims/{id}` returns full details of a single claim with ownership check
- Returns 403 if claim belongs to another user, 404 if not found
- Pagination supported via `page` and `size` query params

## 3. API Details

### GET /api/claims/v1/claims
- **URL:** `http://localhost:8082/api/claims/v1/claims?page=0&size=10`
- **Headers:** `Authorization: Bearer <JWT>`

**Response (200):**
```json
{
  "timestamp": "2026-07-10T12:05:00",
  "status": 200,
  "message": "Claims retrieved",
  "data": {
    "content": [
      {
        "claimId": "c1d2e3f4-a5b6-7890-abcd-ef1234567890",
        "policyNumber": "POL-2026-00123",
        "claimType": "MEDICAL",
        "status": "SUBMITTED",
        "claimedAmount": 75000.00,
        "submittedAt": "2026-07-10T12:00:00"
      }
    ],
    "pageable": {"pageNumber": 0, "pageSize": 10},
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### GET /api/claims/v1/claims/{id}
- **URL:** `http://localhost:8082/api/claims/v1/claims/{claimId}`
- **Headers:** `Authorization: Bearer <JWT>`

**Response (200):** Full claim details including userId, incidentDate, description
**Response (403):** Claim belongs to different user
**Response (404):** Claim ID does not exist

## 4. Database Changes
- No new tables. Uses existing `claims.claims` table.

## 5. Data Inserted
- No seed data.

## 6. Postman Testing Guide

### Test 1: List My Claims
```json
{
  "method": "GET",
  "url": "http://localhost:8082/api/claims/v1/claims?page=0&size=10",
  "headers": {
    "Authorization": "Bearer {{token}}"
  },
  "expectedStatus": 200
}
```

### Test 2: Get Specific Claim
```json
{
  "method": "GET",
  "url": "http://localhost:8082/api/claims/v1/claims/{{claimId}}",
  "headers": {
    "Authorization": "Bearer {{token}}"
  },
  "expectedStatus": 200
}
```

### Test 3: Get Non-Existent Claim (404)
```json
{
  "method": "GET",
  "url": "http://localhost:8082/api/claims/v1/claims/00000000-0000-0000-0000-000000000000",
  "headers": {
    "Authorization": "Bearer {{token}}"
  },
  "expectedStatus": 404
}
```

## 7. Test Coverage
- `ClaimServiceImplTest.should_returnClaim_when_userOwnsIt()`
- `ClaimServiceImplTest.should_throwAccessDenied_when_userDoesNotOwnClaim()`
- `ClaimServiceImplTest.should_throwNotFound_when_claimDoesNotExist()`
- `ClaimServiceImplTest.should_returnPageOfClaims_when_getAllClaims()`

## 8. Notes / Assumptions
- User ID extracted from JWT token for ownership filtering
- Claims are filtered by userId at the repository level
