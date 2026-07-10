# Execution Report – US-013: Notify on Claim Submitted and Fraud Analysed

## 1. User Story Summary
- **ID:** US-013
- **Title:** Notify on Claim Submitted and Fraud Analysed
- **Description:** Users are notified via simulated EMAIL and SMS when their claim is submitted and when fraud analysis completes.

## 2. Functional Overview
- `NotificationEventHandler` listens for `ClaimCreatedEvent` and `FraudAnalysisCompletedEvent`
- For each event: simulated email and SMS logged to console
- Two notification records saved per event (EMAIL + SMS) = 4 records total for submit+fraud
- Notifications persisted to `notifications.notifications` table (in claims-service schema)

## 3. API Details
- **No REST endpoints** — operates via internal Spring Application Events
- **Triggers:** `ClaimCreatedEvent`, `FraudAnalysisCompletedEvent`
- **Console output:** `[EMAIL] To: {userId} | Subject: ... | Body: ...` and `[SMS] To: {userId} | Message: ...`

## 4. Database Changes
- **Table:** `notifications.notifications` (in claims-service DB)
- **Columns:** id (UUID PK), user_id, claim_id, type, channel, message, sent_at

## 5. Data Inserted
- No seed data. Notifications created automatically on events.

## 6. Postman Testing Guide
Submit a claim and verify notifications were created via admin endpoint:

### Step 1: Submit a Claim (triggers ClaimCreated + FraudAnalysisCompleted)
```json
{
  "method": "POST",
  "url": "http://localhost:8082/api/claims/v1/claims",
  "headers": {
    "Authorization": "Bearer {{userToken}}",
    "Content-Type": "application/json"
  },
  "body": {
    "policyNumber": "POL-2026-NOTIFY",
    "claimType": "PROPERTY",
    "incidentDate": "2026-06-18",
    "description": "Fire damage to property",
    "claimedAmount": 25000.00,
    "policyAgeMonths": 8
  }
}
```

### Step 2: Verify Notifications via Admin
```json
{
  "method": "GET",
  "url": "http://localhost:8084/api/notifications/v1/admin/notifications/{{userId}}?page=0&size=10",
  "headers": {
    "Authorization": "Bearer {{adminToken}}"
  },
  "expectedResponse": {
    "content": [
      {"type": "CLAIM_SUBMITTED", "channel": "EMAIL"},
      {"type": "CLAIM_SUBMITTED", "channel": "SMS"},
      {"type": "FRAUD_ANALYSED", "channel": "EMAIL"},
      {"type": "FRAUD_ANALYSED", "channel": "SMS"}
    ]
  }
}
```

## 7. Test Coverage
- Event-driven flow tested end-to-end via integration
- NotificationEventHandler logs verified in console output during tests

## 8. Notes / Assumptions
- Notifications are simulated (console log) — no actual email/SMS sent
- Each event produces exactly 2 DB records (one per channel)
