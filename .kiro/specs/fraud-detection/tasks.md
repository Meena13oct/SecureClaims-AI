# Fraud Detection Service – Tasks

## US-011: Fraud Rule Engine & Analysis Persistence
- [x] Create `FraudEventHandler` with @EventListener for ClaimCreatedEvent
- [x] Implement `FraudRuleEngine.calculateScore()` with 3 rules:
  - [x] Rule 1: Claimed amount > $50,000 → +3 pts; $10,000–$50,000 → +1 pt
  - [x] Rule 2: Policy age < 6 months → +2 pts
  - [x] Rule 3: Prior claims (12 months) > 3 → +3 pts
- [x] Map total score to RiskLevel: 0–1 = LOW, 2–3 = MEDIUM, 4+ = HIGH
- [x] Create `FraudAnalysis` entity with all required fields
- [x] Persist result to `fraud.fraud_analyses` table
- [x] Generate `analysisNotes` with readable breakdown of rule contributions
- [x] Publish `FraudAnalysisCompletedEvent` after persist
- [x] Make scoring thresholds configurable via application.yml
- [x] Unit test: each rule at boundary values (below/at/above threshold)

## US-015: Admin: View Fraud Analysis
- [x] Implement `GET /admin/fraud/{claimId}` in AdminFraudController
- [x] Return claimId, riskScore, riskLevel, analysisNotes, analyzedAt
- [x] Return 404 if no fraud analysis exists for given claimId
- [x] Require ADMIN JWT — return 403 for USER tokens
- [x] Apply @PreAuthorize("hasRole('ADMIN')")

## US-017: Global Exception Handler (Fraud Detection Service)
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
- [x] Swagger UI accessible at http://localhost:8083/swagger-ui.html
