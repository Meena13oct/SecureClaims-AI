# Fraud Detection Service – Design

## Architecture

- **Service:** Fraud Detection Service
- **Port:** 8083
- **Base URL:** `http://localhost:8083/api/fraud/v1`
- **Schema:** `fraud`
- **Technology:** Java 17, Spring Boot 3.x, Spring ApplicationEventListener, Spring Data JPA, PostgreSQL

## Component Architecture

```
┌───────────────────────────────────────────────────────┐
│         Fraud Detection Service (:8083)                │
│                                                       │
│  Controller Layer                                     │
│  └── AdminFraudController (view fraud analysis)       │
│                                                       │
│  Service Layer                                        │
│  ├── FraudService (analyze, persist, publish)         │
│  ├── FraudRuleEngine (scoring calculation)            │
│  └── FraudEventHandler (@EventListener)               │
│                                                       │
│  Repository Layer                                     │
│  └── FraudAnalysisRepository                          │
│                                                       │
│  Events                                               │
│  ├── Consumes: ClaimCreatedEvent                      │
│  └── Publishes: FraudAnalysisCompletedEvent           │
│                                                       │
│  Database: PostgreSQL (schema: fraud)                 │
│  └── fraud_analyses                                   │
└───────────────────────────────────────────────────────┘
```

## Event-Driven Flow

```
ClaimCreatedEvent received
    │
    ▼
FraudEventHandler (@EventListener)
    │
    ▼
FraudRuleEngine.calculateScore()
    ├── Rule 1: Claimed Amount scoring
    ├── Rule 2: Policy Age scoring
    └── Rule 3: Prior Claims History scoring
    │
    ▼
Map total score → RiskLevel (LOW/MEDIUM/HIGH)
    │
    ▼
Persist FraudAnalysis entity to fraud.fraud_analyses
    │
    ▼
Publish FraudAnalysisCompletedEvent
    ├──► Claims Service (auto-update claim status)
    └──► Notification Service (notify user)
```

## API Endpoints

### `GET /api/fraud/v1/admin/fraud/{claimId}` (ADMIN)
Retrieve the fraud analysis result for a specific claim.

**Response (200):**
```json
{
  "timestamp": "2026-07-04T12:00:00Z",
  "status": 200,
  "message": "Request processed successfully",
  "data": {
    "claimId": "c1d2e3f4-a5b6-7890-abcd-ef1234567890",
    "riskScore": 5,
    "riskLevel": "HIGH",
    "analysisNotes": "Claimed amount > $50,000 (+3 pts); Policy age < 6 months (+2 pts)",
    "analyzedAt": "2026-07-04T11:01:00Z"
  }
}
```

**Error Responses:** 401 (no JWT), 403 (USER role), 404 (no analysis found)

## Scoring Configuration (application.yml)

```yaml
app:
  fraud:
    rules:
      high-amount-threshold: 50000
      medium-amount-threshold: 10000
      high-amount-points: 3
      medium-amount-points: 1
      policy-age-threshold-months: 6
      policy-age-points: 2
      prior-claims-threshold: 3
      prior-claims-points: 3
    risk-levels:
      low-max-score: 1
      medium-max-score: 3
```

## Requirement → Component Mapping

| Req ID | Component | Mechanism |
|--------|-----------|-----------|
| FR-029 | FraudEventHandler | @EventListener ClaimCreatedEvent |
| FR-030 | FraudRuleEngine | Three-rule scoring calculation |
| FR-031 | FraudRuleEngine | Score threshold → RiskLevel enum |
| FR-032 | FraudRuleEngine.calculateScore() | Configurable thresholds |
| FR-033 | FraudAnalysisRepository | FraudAnalysis entity → fraud schema |
| FR-034 | FraudService → ApplicationEventPublisher | FraudAnalysisCompletedEvent |
| FR-035 | AdminFraudController | GET /admin/fraud/{claimId} |
| FR-036 | Claims Service (consumer) | Status → REJECTED if HIGH |
| FR-037 | Claims Service (consumer) | Status → UNDER_REVIEW if LOW/MEDIUM |
