-- ============================================================
-- SecureClaims AI - Fraud Detection Service Database Script
-- Schema: fraud
-- Tables: fraud_analyses
-- ============================================================

-- Create schema if not exists
CREATE SCHEMA IF NOT EXISTS fraud;

-- Set search path
SET search_path TO fraud;

-- ============================================================
-- Table: fraud_analyses
-- Stores the result of rule-based fraud risk scoring per claim
-- ============================================================
CREATE TABLE IF NOT EXISTS fraud_analyses (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    claim_id            UUID            NOT NULL UNIQUE,
    user_id             UUID            NOT NULL,
    claimed_amount      DECIMAL(15, 2)  NOT NULL,
    policy_age_months   INTEGER         NOT NULL,
    prior_claims_count  INTEGER         NOT NULL,
    risk_score          INTEGER         NOT NULL,
    risk_level          VARCHAR(10)     NOT NULL,
    analysis_notes      TEXT,
    analyzed_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for claim lookup (also enforced by UNIQUE constraint)
CREATE INDEX IF NOT EXISTS idx_fraud_analyses_claim_id ON fraud_analyses (claim_id);

-- Index for user history queries (counting prior claims)
CREATE INDEX IF NOT EXISTS idx_fraud_analyses_user_id ON fraud_analyses (user_id);

-- Index for filtering by risk level (admin queries)
CREATE INDEX IF NOT EXISTS idx_fraud_analyses_risk_level ON fraud_analyses (risk_level);

-- Constraint: risk_level must be one of LOW, MEDIUM, HIGH
ALTER TABLE fraud_analyses
    ADD CONSTRAINT chk_risk_level CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH'));
