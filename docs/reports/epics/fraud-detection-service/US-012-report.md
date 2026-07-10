# Execution Report – US-012: Automatic Claim Status Update from Fraud Result

## 1. User Story Summary
- **ID:** US-012
- **Title:** Automatic Claim Status Update from Fraud Result
- **Description:** Claims Service listens for `FraudAnalysisCompletedEvent` and automatically updates claim status based on risk level.

## 2. Functional Overview
- HIGH risk → claim status set to REJECTED (updatedBy = "FRAUD_ENGINE")
- LOW or MEDIUM risk → claim status set to UNDER_REVIEW (updatedBy = "FRAUD_ENGINE")
- Status update is transactional
- `ClaimStatusUpdatedEvent` published after status change (triggers notifications)
- Implemented in `FraudEventHandler` (inline) and `ClaimStatusEventHandler`

## 3. API Details
- **No REST endpoints** — operates via internal Spring Application Events
- **Trigger:** `FraudAnalysisCompletedEvent` from fraud scoring
- **Output:** `ClaimStatusUpdatedEvent` published after claim status change

**Auto-Update Logic:**
| Risk Level | New Claim Status | Updated By |
|------------|-----------------|------------|
| HIGH | REJECTED | FRAUD_ENGINE |
| MEDIUM | UNDER_REVIEW | FRAUD_ENGINE |
| LOW | UNDER_REVIEW | FRAUD_ENGINE |

## 4. Database Changes
- Updates `status` and `updated_by` columns in `claims.claims`

## 5. Data Inserted
- No seed data.

## 6. Postman Testing Guide
Submit a high-value claim and verify status becomes REJECTED:

### Step 1: Submit Claim (amount > $50K, policy < 6 months)
```json
{
  "method": "POST",
  "url": "http://localhost:8082/api/claims/v1/claims",
  "headers": {
    "Authorization": "Bearer {{userToken}}",
    "Content-Type": "application/json"
  },
  "body": {
    "policyNumber": "POL-2026-REJECT",
    "claimType": "AUTO",
    "incidentDate": "2026-06-15",
    "description": "High risk claim to test auto-rejection",
    "claimedAmount": 60000.00,
    "policyAgeMonths": 2
  }
}
```

### Step 2: Verify Claim Status is REJECTED
```json
{
  "method": "GET",
  "url": "http://localhost:8082/api/claims/v1/claims/{{claimId}}",
  "headers": {
    "Authorization": "Bearer {{userToken}}"
  },
  "expectedResponse": {
    "status": "REJECTED"
  }
}
```

## 7. Test Coverage
- `FraudEventHandlerTest.should_rejectClaim_when_riskIsHigh()` — verifies REJECTED status on HIGH risk
- `ClaimServiceImplTest.should_updateStatus_when_validTransition()` — status update mechanics

## 8. Notes / Assumptions
- Auto-update runs in same transaction as fraud analysis (REQUIRES_NEW propagation)
- If claim is already at REJECTED, duplicate event won't cause issues (idempotent)
