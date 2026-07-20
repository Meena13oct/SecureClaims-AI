# Claims Processing Service – Requirements

## Overview
Core service managing the entire insurance claim lifecycle — from submission through document upload, fraud evaluation, status transitions, and final resolution. Acts as the central event publisher for downstream services.

## Functional Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-014 | The system shall allow an authenticated USER to submit a new insurance claim via POST /claims. | High |
| FR-015 | A claim submission shall require: policy number, claim type, incident date, description, and claimed amount. | High |
| FR-016 | The system shall assign a unique claim ID and set initial status to SUBMITTED upon claim creation. | High |
| FR-017 | The system shall allow an authenticated USER to retrieve all their own submitted claims via GET /claims. | High |
| FR-018 | The system shall allow an authenticated USER to retrieve a specific claim by ID via GET /claims/{id}. | High |
| FR-019 | The system shall allow an ADMIN to retrieve all claims across all users via GET /admin/claims. | High |
| FR-020 | The system shall allow an ADMIN to manually update the status of any claim via PUT /admin/claims/{id}/status. | High |
| FR-021 | Valid claim statuses shall be: SUBMITTED, UNDER_REVIEW, FRAUD_CHECK, APPROVED, REJECTED, CLOSED. | High |
| FR-022 | The system shall allow an authenticated USER to upload PDF documents for a claim via POST /claims/{id}/documents. | High |
| FR-023 | Uploaded documents shall be stored in the local file system under uploads/claims/{claimId}/. | High |
| FR-024 | The system shall store only file metadata (filename, file path, MIME type, upload timestamp) in the database. | High |
| FR-025 | The system shall reject document uploads that are not PDF or supported image formats, returning 400 Bad Request. | Medium |
| FR-026 | The system shall allow an authenticated USER to list all documents associated with their claim via GET /claims/{id}/documents. | Medium |
| FR-027 | The system shall publish a ClaimCreatedEvent after a claim is successfully persisted. | High |
| FR-028 | The system shall publish a ClaimStatusUpdatedEvent whenever a claim's status changes. | High |

## Non-Functional Requirements

| ID | Requirement |
|----|-------------|
| NFR-008 | REST API endpoints shall respond within 500ms for read operations under normal load. |
| NFR-009 | Claim submission and fraud analysis shall complete end-to-end within 3 seconds. |
| NFR-007 | File uploads shall be validated for type and size before being written to the file system. |
| NFR-016 | All REST APIs shall be documented using OpenAPI 3.0 / Swagger UI. |

## Data Entities

### Entity: `claims` (Schema: `claims`)

| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK, NOT NULL |
| user_id | UUID | NOT NULL |
| policy_number | VARCHAR(50) | NOT NULL |
| claim_type | VARCHAR(50) | NOT NULL |
| incident_date | DATE | NOT NULL |
| description | TEXT | NOT NULL |
| claimed_amount | DECIMAL(15,2) | NOT NULL |
| status | VARCHAR(30) | NOT NULL |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

### Entity: `documents`

| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK, NOT NULL |
| claim_id | UUID | FK → claims.id, NOT NULL |
| original_filename | VARCHAR(255) | NOT NULL |
| stored_filename | VARCHAR(255) | NOT NULL |
| file_path | VARCHAR(500) | NOT NULL |
| mime_type | VARCHAR(100) | NOT NULL |
| file_size_bytes | BIGINT | NOT NULL |
| uploaded_at | TIMESTAMP | NOT NULL |

## Events Published

- `ClaimCreatedEvent` — on successful claim submission
- `ClaimStatusUpdatedEvent` — on any status transition

## Events Consumed

- `FraudAnalysisCompletedEvent` — auto-updates claim status based on risk level
