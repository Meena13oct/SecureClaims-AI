# Notification Service – Requirements

## Overview
User communication layer that delivers status updates and alerts. Listens to domain events from Claims and Fraud Detection services, simulates email/SMS delivery via console logging, and persists notification records for audit.

## Functional Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-038 | The system shall send a notification to the user when their claim is successfully submitted (ClaimCreatedEvent). | High |
| FR-039 | The system shall send a notification to the user when fraud analysis is completed (FraudAnalysisCompletedEvent). | High |
| FR-040 | The system shall send a notification to the user whenever their claim status changes (ClaimStatusUpdatedEvent). | High |
| FR-041 | Notifications shall be delivered via simulated email and SMS output to the application console/logs. | High |
| FR-042 | The system shall persist notification records (type, recipient, message, timestamp, delivery status) in the database. | Medium |
| FR-043 | The system shall allow an ADMIN to retrieve notification history for any user via GET /admin/notifications/{userId}. | Low |
| FR-044 | The Notification Service shall be designed with an abstraction layer to allow future integration with real email/SMS providers (e.g., SendGrid, Twilio). | Low |

## Non-Functional Requirements

| ID | Requirement |
|----|-------------|
| NFR-012 | The system shall implement a retry mechanism for failed event deliveries. |
| NFR-014 | The system shall log all application errors at ERROR level with stack traces using SLF4J + Logback. |
| NFR-021 | All event publishing and handling logic shall have dedicated unit tests. |
| NFR-016 | All REST APIs shall be documented using OpenAPI 3.0 / Swagger UI. |

## Notification Types

| Type | Trigger Event | Message |
|------|--------------|---------|
| CLAIM_RECEIVED | ClaimCreatedEvent | "Your claim #{claimId} has been received." |
| FRAUD_ANALYSIS_DONE | FraudAnalysisCompletedEvent | "Your claim is under review." / "Your claim has been flagged." |
| STATUS_UPDATED | ClaimStatusUpdatedEvent | "Your claim status changed from {prev} to {new}." |

## Delivery Channels

| Channel | Implementation |
|---------|---------------|
| EMAIL | Simulated via SLF4J log: `[EMAIL] To: {userId} | Subject: ... | Body: ...` |
| SMS | Simulated via SLF4J log: `[SMS] To: {userId} | Message: ...` |

## Data Entity

### Entity: `notifications` (Schema: `notifications`)

| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK, NOT NULL |
| user_id | UUID | NOT NULL |
| claim_id | UUID | |
| notification_type | VARCHAR(50) | NOT NULL |
| channel | VARCHAR(10) | NOT NULL |
| recipient_address | VARCHAR(255) | NOT NULL |
| subject | VARCHAR(255) | |
| message_body | TEXT | NOT NULL |
| delivery_status | VARCHAR(10) | NOT NULL |
| sent_at | TIMESTAMP | NOT NULL |

## Events Consumed

- `ClaimCreatedEvent` — triggers "Claim Received" notification (EMAIL + SMS)
- `FraudAnalysisCompletedEvent` — triggers "Fraud Analysis Done" notification (EMAIL + SMS)
- `ClaimStatusUpdatedEvent` — triggers "Status Updated" notification (EMAIL + SMS)

## Design Pattern

- `NotificationSender` interface abstracts delivery channels
- `ConsoleEmailSender` and `ConsoleSMSSender` implement the interface
- Future real providers (SendGrid, Twilio) can be swapped without changing event handlers
