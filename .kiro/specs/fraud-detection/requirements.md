# Fraud Detection Service – Requirements

## Overview
Automated rule-based risk assessment engine for submitted insurance claims. Consumes claim events, applies configurable scoring rules, persists analysis results, and publishes completion events that drive automatic claim status updates.

## Functional Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-029 | The system shall evaluate every new claim for fraud risk upon receiving a ClaimCreatedEvent. | High |
| FR-030 | The fraud engine shall assess risk based on three factors: claimed amount, policy age, and user claim history. | High |
| FR-031 | The fraud engine shall classify each claim as LOW, MEDIUM, or HIGH risk based on the combined score. | High |
| FR-032 | Risk scoring rules: amount > $50,000 adds high weight; policy age < 6 months adds medium weight; more than 3 prior claims in 12 months adds high weight. | High |
| FR-033 | The system shall persist the fraud analysis result (risk score, risk level, analysis timestamp) linked to the claim ID. | High |
| FR-034 | The system shall publish a FraudAnalysisCompletedEvent after completing the fraud analysis. | High |
| FR-035 | The system shall allow an ADMIN to retrieve the fraud analysis result for any claim via GET /admin/fraud/{claimId}. | Medium |
| FR-036 | Claims flagged as HIGH risk shall automatically transition to REJECTED status unless overridden by an ADMIN. | Medium |
| FR-037 | Claims flagged as LOW or MEDIUM risk shall transition to UNDER_REVIEW status for further processing. | High |

## Non-Functional Requirements

| ID | Requirement |
|----|-------------|
| NFR-009 | Claim submission and fraud analysis shall complete end-to-end within 3 seconds. |
| NFR-012 | The system shall implement a retry mechanism for failed event deliveries. |
| NFR-021 | All event publishing and handling logic shall have dedicated unit tests. |

## Risk Scoring Rules

| Rule | Condition | Points |
|------|-----------|--------|
| Claimed Amount | > $50,000 | +3 |
| Claimed Amount | $10,000 – $50,000 | +1 |
| Policy Age | < 6 months | +2 |
| Prior Claims (12 months) | > 3 claims | +3 |

**Risk Level Mapping:**

| Score | Risk Level |
|-------|------------|
| 0 – 1 | LOW |
| 2 – 3 | MEDIUM |
| 4+ | HIGH |

## Data Entity

### Entity: `fraud_analyses` (Schema: `fraud`)

| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK, NOT NULL |
| claim_id | UUID | UNIQUE, NOT NULL |
| user_id | UUID | NOT NULL |
| claimed_amount | DECIMAL(15,2) | NOT NULL |
| policy_age_months | INTEGER | NOT NULL |
| prior_claims_count | INTEGER | NOT NULL |
| risk_score | INTEGER | NOT NULL |
| risk_level | VARCHAR(10) | NOT NULL |
| analysis_notes | TEXT | |
| analyzed_at | TIMESTAMP | NOT NULL |

## Events Consumed

- `ClaimCreatedEvent` — triggers fraud analysis scoring

## Events Published

- `FraudAnalysisCompletedEvent` — after analysis is persisted, consumed by Claims Service and Notification Service
