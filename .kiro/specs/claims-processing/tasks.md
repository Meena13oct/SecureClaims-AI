# Claims Processing Service – Tasks

## US-007: Submit a Claim
- [x] Create `ClaimRequest` DTO with validation (@NotBlank, @NotNull, @Min, @PastOrPresent)
- [x] Create `ClaimResponse` DTO
- [x] Implement `ClaimService.createClaim()` — generate UUID, set SUBMITTED, persist
- [x] Implement `POST /claims` in ClaimController with @Valid
- [x] Extract userId from JWT SecurityContext
- [x] Publish `ClaimCreatedEvent` after successful persist
- [x] Return 201 Created with claimId, status, submittedAt
- [x] Unit test: create claim success, ClaimCreatedEvent published

## US-008: View Claims
- [x] Implement `GET /claims` — return all claims filtered by userId from JWT
- [x] Add pagination support (?page=0&size=10)
- [x] Implement `GET /claims/{id}` — return single claim with ownership check
- [x] Return 403 Forbidden if claim belongs to different user
- [x] Return 404 Not Found if claim ID doesn't exist

## US-009: Admin: View and Update Claims
- [x] Implement `GET /admin/claims` — return all claims with pagination (ADMIN only)
- [x] Implement `PUT /admin/claims/{id}/status` — update claim status (ADMIN only)
- [x] Validate status transitions (SUBMITTED → UNDER_REVIEW → APPROVED/REJECTED → CLOSED)
- [x] Return 400 for invalid transitions
- [x] Publish `ClaimStatusUpdatedEvent` after successful update
- [x] Apply @PreAuthorize("hasRole('ADMIN')") on AdminClaimController
- [x] Unit test: valid transition, invalid transition rejected

## US-010: Upload and List Documents
- [x] Implement `POST /claims/{id}/documents` — multipart file upload
- [x] Validate MIME type (only application/pdf allowed)
- [x] Validate file size (max 10 MB)
- [x] Save file to `uploads/claims/{claimId}/` on local filesystem
- [x] Persist document metadata (filename, path, MIME type, size, timestamp)
- [x] Enforce claim ownership — return 403 if not owner
- [x] Implement `GET /claims/{id}/documents` — list documents for a claim
- [x] Return 201 Created with documentId, originalFilename, filePath, uploadedAt

## US-012: Automatic Claim Status Update from Fraud Result
- [x] Create `@EventListener` for `FraudAnalysisCompletedEvent` in ClaimService
- [x] HIGH risk → update claim status to REJECTED (updatedBy = "FRAUD_ENGINE")
- [x] LOW/MEDIUM risk → update claim status to UNDER_REVIEW (updatedBy = "FRAUD_ENGINE")
- [x] Publish `ClaimStatusUpdatedEvent` after auto-update
- [x] Wrap in @Transactional

## US-017: Global Exception Handler (Claims Service)
- [x] Create `GlobalExceptionHandler` with @RestControllerAdvice
- [x] Handle MethodArgumentNotValidException → 400
- [x] Handle ResourceNotFoundException → 404
- [x] Handle AccessDeniedException → 403
- [x] Handle generic Exception → 500 (no stack trace)
- [x] Standard error shape: timestamp, status, error, message, path

## US-018: Swagger UI
- [x] Add springdoc-openapi-starter-webmvc-ui dependency
- [x] Configure JWT Bearer security scheme
- [x] Add @Operation annotations on all controller methods
- [x] Swagger UI accessible at http://localhost:8082/swagger-ui.html
