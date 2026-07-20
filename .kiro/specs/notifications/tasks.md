# Notification Service – Tasks

## US-013: Notify on Claim Submitted and Fraud Analysed
- [x] Create `NotificationEventHandler` with @EventListener for ClaimCreatedEvent
- [x] Create @EventListener for FraudAnalysisCompletedEvent
- [x] Log simulated email: `[EMAIL] To: {userId} | Subject: ... | Body: ...`
- [x] Log simulated SMS: `[SMS] To: {userId} | Message: ...`
- [x] Create `Notification` entity with all required fields
- [x] Save 2 notification records per event (EMAIL + SMS channel)
- [x] For FraudAnalysisCompleted: customize message based on risk level (HIGH vs LOW/MEDIUM)
- [x] Unit test: event handler creates correct notification records

## US-014: Notify on Status Change
- [x] Create @EventListener for ClaimStatusUpdatedEvent
- [x] Include previous status and new status in notification message
- [x] Log simulated email and SMS to console
- [x] Save 2 notification records (EMAIL + SMS) with type = "STATUS_UPDATED"
- [x] Trigger for both admin manual updates and fraud engine auto-updates

## US-015: Admin: View Notification History
- [x] Implement `GET /admin/notifications/{userId}` in AdminNotificationController
- [x] Return all notification records for given userId with pagination
- [x] Include notificationId, type, channel, message, sentAt in each record
- [x] Require ADMIN JWT — return 403 for USER tokens
- [x] Apply @PreAuthorize("hasRole('ADMIN')")

## US-017: Global Exception Handler (Notification Service)
- [x] Create `GlobalExceptionHandler` with @RestControllerAdvice
- [x] Handle MethodArgumentNotValidException → 400
- [x] Handle ResourceNotFoundException → 404
- [x] Handle AccessDeniedException → 403
- [x] Handle generic Exception → 500 (no stack trace)
- [x] Standard error shape: timestamp, status, error, message, path

## US-018: Swagger UI
- [x] Add springdoc-openapi-starter-webmvc-ui dependency
- [x] Configure JWT Bearer security scheme
- [x] Add @Operation annotations on admin controller methods
- [x] Swagger UI accessible at http://localhost:8084/swagger-ui.html
