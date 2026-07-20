# Notification Service – Design

## Architecture

- **Service:** Notification Service
- **Port:** 8084
- **Base URL:** `http://localhost:8084/api/notifications/v1`
- **Schema:** `notifications`
- **Technology:** Java 17, Spring Boot 3.x, Spring ApplicationEventListener, Spring Data JPA, PostgreSQL, SLF4J + Logback

## Component Architecture

```
┌─────────────────────────────────────────────────────────────┐
│            Notification Service (:8084)                       │
│                                                             │
│  Controller Layer                                           │
│  └── AdminNotificationController (notification history)     │
│                                                             │
│  Service Layer                                              │
│  ├── NotificationService (create, persist, send)            │
│  └── NotificationEventHandler (@EventListener x 3)          │
│                                                             │
│  Sender Abstraction (Strategy Pattern)                      │
│  ├── NotificationSender (interface)                         │
│  ├── ConsoleEmailSender (simulated email via SLF4J)         │
│  └── ConsoleSMSSender (simulated SMS via SLF4J)             │
│                                                             │
│  Repository Layer                                           │
│  └── NotificationRepository                                 │
│                                                             │
│  Events Consumed                                            │
│  ├── ClaimCreatedEvent                                      │
│  ├── FraudAnalysisCompletedEvent                            │
│  └── ClaimStatusUpdatedEvent                                │
│                                                             │
│  Database: PostgreSQL (schema: notifications)               │
│  └── notifications                                          │
└─────────────────────────────────────────────────────────────┘
```

## Event-Driven Flow

```
ClaimCreatedEvent received
    │
    ▼
NotificationEventHandler.onClaimCreated()
    ├── ConsoleEmailSender.send() → log [EMAIL] "Claim received"
    ├── ConsoleSMSSender.send() → log [SMS] "Claim received"
    └── Persist 2 Notification records (EMAIL + SMS)

FraudAnalysisCompletedEvent received
    │
    ▼
NotificationEventHandler.onFraudAnalysisCompleted()
    ├── If HIGH: "Your claim has been flagged for review"
    ├── If LOW/MEDIUM: "Your claim is under review"
    ├── ConsoleEmailSender.send() + ConsoleSMSSender.send()
    └── Persist 2 Notification records

ClaimStatusUpdatedEvent received
    │
    ▼
NotificationEventHandler.onClaimStatusUpdated()
    ├── Message: "Status changed from {prev} to {new}"
    ├── ConsoleEmailSender.send() + ConsoleSMSSender.send()
    └── Persist 2 Notification records
```

## API Endpoints

### `GET /api/notifications/v1/admin/notifications/{userId}` (ADMIN)
Retrieve the full notification history for a specific user.

**Query Parameters:** `?page=0&size=10`

**Response (200):**
```json
{
  "timestamp": "2026-07-04T12:05:00Z",
  "status": 200,
  "message": "Request processed successfully",
  "data": {
    "content": [
      {
        "notificationId": "uuid",
        "type": "CLAIM_SUBMITTED",
        "channel": "EMAIL",
        "message": "Your claim POL-2026-00123 has been submitted.",
        "sentAt": "2026-07-04T11:00:30Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 4,
    "totalPages": 1
  }
}
```

**Error Responses:** 401 (no JWT), 403 (USER role)

## Console Output Format

```
[EMAIL] To: jane.doe@example.com | Subject: Claim Received | Body: Your claim #c1d2e3f4 has been received and is being processed.
[SMS] To: +1-555-0100 | Message: SecureClaims: Your claim #c1d2e3f4 has been received.
```

## Requirement → Component Mapping

| Req ID | Component | Mechanism |
|--------|-----------|-----------|
| FR-038 | NotificationEventHandler | @EventListener ClaimCreatedEvent |
| FR-039 | NotificationEventHandler | @EventListener FraudAnalysisCompletedEvent |
| FR-040 | NotificationEventHandler | @EventListener ClaimStatusUpdatedEvent |
| FR-041 | ConsoleEmailSender, ConsoleSMSSender | SLF4J log.info() |
| FR-042 | NotificationRepository | Notification entity → notifications schema |
| FR-043 | AdminNotificationController | GET /admin/notifications/{userId} |
| FR-044 | NotificationSender interface | Strategy pattern, injectable |
