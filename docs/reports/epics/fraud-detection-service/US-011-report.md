# Execution Report – US-011: Fraud Rule Engine & Analysis Persistence

## 1. User Story Summary
- **ID:** US-011
- **Title:** Fraud Rule Engine & Analysis Persistence
- **Description:** Fraud Detection Service automatically scores claims when submitted and saves results using configurable rules.

## 2. Functional Overview
- `FraudEventHandler` listens for `ClaimCreatedEvent` via `@TransactionalEventListener`
- Three scoring rules applied:
  - Rule 1 (Amount): >$50,000 → +3 pts; $10,000–$50,000 → +1 pt
  - Rule 2 (Policy Age): <6 months → +2 pts
  - Rule 3 (Prior Claims): >3 claims in last 12 months → +3 pts
- Score mapped to RiskLevel: 0–1=LOW, 2–3=MEDIUM, 4+=HIGH
- Result saved to `fraud.fraud_analyses` table
- `FraudAnalysisCompletedEvent` published after save

## 3. API Details
- **No REST endpoints** — operates via internal Spring Application Events
- **Trigger:** `ClaimCreatedEvent` published when a claim is submitted
- **Output:** `FraudAnalysisCompletedEvent` published after analysis saved

## 4. Database Changes
- **Table:** `fraud.fraud_analyses` (in claims-service schema for monolith event handling)
- **Columns:** id (UUID PK), claim_id (unique), user_id, risk_score, risk_level, analysis_notes, analyzed_at

## 5. Data Inserted
- No seed data. Analysis created automatically per claim submission.

## 6. Postman Testing Guide
To verify fraud analysis, submit a claim then check via admin endpoint:

### Step 1: Submit High-Value Claim
```json
{
  "method": "POST",
  "url": "http://localhost:8082/api/claims/v1/claims",
  "headers": {
    "Authorization": "Bearer {{userToken}}",
    "Content-Type": "application/json"
  },
  "body": {
    "policyNumber": "POL-2026-HIGH",
    "claimType": "MEDICAL",
    "incidentDate": "2026-06-20",
    "description": "High value claim for fraud test",
    "claimedAmount": 60000.00,
    "policyAgeMonths": 3
  }
}
```

### Step 2: Verify Fraud Analysis via Admin
```json
{
  "method": "GET",
  "url": "http://localhost:8083/api/fraud/v1/admin/fraud/{{claimId}}",
  "headers": {
    "Authorization": "Bearer {{adminToken}}"
  },
  "expectedResponse": {
    "riskScore": 5,
    "riskLevel": "HIGH",
    "analysisNotes": "Amount rule: +3...; Policy age rule: +2..."
  }
}
```

## 7. Test Coverage
- `FraudEventHandlerTest.should_scoreHigh_when_amountExceeds50000()` — amount >$50K + young policy
- `FraudEventHandlerTest.should_scoreMedium_when_amountBetween10000And50000()` — medium amount
- `FraudEventHandlerTest.should_scoreLow_when_allRulesBelowThreshold()` — all rules below
- `FraudEventHandlerTest.should_addPriorClaimsPoints_when_moreThan3Claims()` — prior claims rule
- `FraudEventHandlerTest.should_publishFraudAnalysisCompletedEvent()` — event publishing

## 8. Notes / Assumptions
- Fraud analysis runs in a new transaction (`REQUIRES_NEW`) after claim commit
- Prior claims count uses `countByUserIdAndSubmittedAtAfter` (last 12 months)
- In production, scoring thresholds should be externalized to `application.yml`
