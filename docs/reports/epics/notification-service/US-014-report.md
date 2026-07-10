# Execution Report – US-014: Notify on Status Change

## 1. User Story Summary
- **ID:** US-014
- **Title:** Notify on Status Change
- **Description:** Users are notified via simulated EMAIL and SMS whenever their claim status changes (both manual admin updates and automatic fraud engine updates).

## 2. Functional Overview
- `NotificationEventHandler` listens for `ClaimStatusUpdatedEvent`
- Message includes previous status and new status
- Simulated email and SMS logged to console
- Two notification records saved: type="STATUS_UPDATED", channels EMAIL and SMS
- Triggers for both admin manual updates (US-009) and fraud engine auto-updates (US-012)

## 3. API Details
- **No REST endpoints** — operates via internal Spring Application Events
- **Trigger:** `ClaimStatusUpdatedEvent`

## 4. Database Changes
- Inserts into `notifications.notifications` with type="STATUS_UPDATED"

## 5. Data Inserted
- No seed data.

## 6. Postman Testing Guide

### Step 1: Admin Updates Claim Status
```json
{
  "method": "PUT",
  "url": "http://localhost:8082/api/claims/v1/admin/claims/{{claimId}}/status",
  "headers": {
    "Authorization": "Bearer {{adminToken}}",
    "Content-Type": "application/json"
  },
  "body": {
    "status": "APPROVED"
  }
}
```

### Step 2: Check Notification History
```json
{
  "method": "GET",
  "url": "http://localhost:8084/api/notifications/v1/admin/notifications/{{userId}}?page=0&size=20",
  "headers": {
    "Authorization": "Bearer {{adminToken}}"
  },
  "expectedResponse": {
    "content": [
      {"type": "STATUS_UPDATED", "channel": "EMAIL", "message": "...UNDER_REVIEW to APPROVED..."},
      {"type": "STATUS_UPDATED", "channel": "SMS"}
    ]
  }
}
```

## 7. Test Coverage
- Event flow verified through integration testing
- Console log output confirms `[EMAIL]` and `[SMS]` messages generated

## 8. Notes / Assumptions
- Status change notifications fire for both manual (admin) and automatic (fraud engine) updates
- Notification message format: "Your claim {id} status changed from {prev} to {new}"
