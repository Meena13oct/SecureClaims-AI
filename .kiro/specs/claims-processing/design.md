# Claims Processing Service – Design

## Architecture

- **Service:** Claims Service
- **Port:** 8082
- **Base URL:** `http://localhost:8082/api/claims/v1`
- **Schema:** `claims`
- **Technology:** Java 17, Spring Boot 3.x, Spring Web, Spring Data JPA, PostgreSQL, Spring ApplicationEventPublisher

## Component Architecture

```
┌─────────────────────────────────────────────────┐
│            Claims Service (:8082)                │
│                                                 │
│  Controller Layer                               │
│  ├── ClaimController (submit, view own claims)  │
│  ├── AdminClaimController (list all, update)    │
│  └── DocumentController (upload, list docs)     │
│                                                 │
│  Service Layer                                  │
│  ├── ClaimService (create, find, updateStatus)  │
│  ├── DocumentService (upload, list)             │
│  ├── FileStorageService (save to filesystem)    │
│  └── ClaimEventHandler (@EventListener)         │
│                                                 │
│  Repository Layer                               │
│  ├── ClaimRepository                            │
│  └── DocumentRepository                         │
│                                                 │
│  Events                                         │
│  ├── Publishes: ClaimCreatedEvent               │
│  ├── Publishes: ClaimStatusUpdatedEvent         │
│  └── Consumes: FraudAnalysisCompletedEvent      │
│                                                 │
│  Database: PostgreSQL (schema: claims)          │
│  ├── claims                                     │
│  └── documents                                  │
│                                                 │
│  File System: uploads/claims/{claimId}/         │
└─────────────────────────────────────────────────┘
```

## API Endpoints

### `POST /api/claims/v1/claims` (USER)
Submit a new insurance claim. Publishes `ClaimCreatedEvent`.

**Request:**
```json
{
  "policyNumber": "POL-2026-00123",
  "claimType": "MEDICAL",
  "incidentDate": "2026-06-20",
  "description": "Hospitalisation due to surgery",
  "claimedAmount": 75000.00
}
```

**Response (201):**
```json
{
  "status": 201,
  "message": "Claim submitted successfully",
  "data": {
    "claimId": "uuid",
    "status": "SUBMITTED",
    "submittedAt": "2026-07-04T11:00:00Z"
  }
}
```

### `GET /api/claims/v1/claims` (USER)
List all claims belonging to the authenticated user. Supports `?page=0&size=10`.

### `GET /api/claims/v1/claims/{id}` (USER)
Get full details of a single claim. Ownership enforced.

### `GET /api/claims/v1/admin/claims` (ADMIN)
List all claims in the system. Supports pagination.

### `PUT /api/claims/v1/admin/claims/{id}/status` (ADMIN)
Update claim status. Publishes `ClaimStatusUpdatedEvent`.

**Valid transitions:**
```
SUBMITTED → UNDER_REVIEW → APPROVED → CLOSED
                         → REJECTED → CLOSED
```

### `POST /api/claims/v1/claims/{id}/documents` (USER)
Upload PDF document (multipart/form-data). Max 10 MB. Only `application/pdf` accepted.

### `GET /api/claims/v1/claims/{id}/documents` (USER)
List all documents for a claim owned by the user.

## Event Flow

```
User submits claim
    │
    ▼
ClaimService persists claim (status: SUBMITTED)
    │
    ▼
Publish ClaimCreatedEvent ──► Fraud Detection Service
                          ──► Notification Service
    │
    ▼ (async)
FraudAnalysisCompletedEvent received
    │
    ▼
ClaimService auto-updates status:
  HIGH risk → REJECTED
  LOW/MEDIUM → UNDER_REVIEW
    │
    ▼
Publish ClaimStatusUpdatedEvent ──► Notification Service
```

## Requirement → Component Mapping

| Req ID | Component | Mechanism |
|--------|-----------|-----------|
| FR-014 | ClaimController → ClaimService | POST /claims |
| FR-015 | ClaimRequest DTO + @Valid | Bean validation |
| FR-016 | ClaimService.createClaim() | UUID + SUBMITTED status |
| FR-017 | ClaimController → ClaimService | GET /claims (filtered by JWT userId) |
| FR-018 | ClaimController → ClaimService | GET /claims/{id} + ownership check |
| FR-019 | AdminClaimController | GET /admin/claims |
| FR-020 | AdminClaimController → ClaimService | PUT /admin/claims/{id}/status |
| FR-021 | ClaimStatus enum | State machine validation |
| FR-022 | DocumentController → DocumentService | POST /claims/{id}/documents |
| FR-023 | FileStorageService | Java NIO Files.copy() |
| FR-024 | Document entity | Metadata only in DB |
| FR-025 | DocumentService + MIME check | 400 for non-PDF |
| FR-026 | DocumentController | GET /claims/{id}/documents |
| FR-027 | ClaimService → ApplicationEventPublisher | ClaimCreatedEvent |
| FR-028 | ClaimService → ApplicationEventPublisher | ClaimStatusUpdatedEvent |
