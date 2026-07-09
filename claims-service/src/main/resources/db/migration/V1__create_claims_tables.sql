-- ============================================================
-- Claims Service Database Migration
-- Schema: claims, fraud, notifications
-- ============================================================

CREATE SCHEMA IF NOT EXISTS claims;
CREATE SCHEMA IF NOT EXISTS fraud;
CREATE SCHEMA IF NOT EXISTS notifications;

SET search_path TO claims;

-- Claims table
CREATE TABLE IF NOT EXISTS claims (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL,
    policy_number   VARCHAR(50)     NOT NULL,
    claim_type      VARCHAR(30)     NOT NULL,
    incident_date   DATE            NOT NULL,
    description     VARCHAR(1000)   NOT NULL,
    claimed_amount  DECIMAL(12,2)   NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'SUBMITTED',
    policy_age_months INT           DEFAULT 12,
    updated_by      VARCHAR(50),
    submitted_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_claims_user_id ON claims (user_id);
CREATE INDEX IF NOT EXISTS idx_claims_status ON claims (status);

-- Documents table
CREATE TABLE IF NOT EXISTS documents (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    claim_id            UUID            NOT NULL REFERENCES claims(id),
    original_filename   VARCHAR(255)    NOT NULL,
    stored_filename     VARCHAR(255)    NOT NULL,
    file_path           VARCHAR(500)    NOT NULL,
    mime_type           VARCHAR(50)     NOT NULL,
    file_size_bytes     BIGINT          NOT NULL,
    uploaded_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_documents_claim_id ON documents (claim_id);

-- Fraud analyses table (in fraud schema, but managed by claims-service)
SET search_path TO fraud;

CREATE TABLE IF NOT EXISTS fraud_analyses (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    claim_id        UUID            NOT NULL UNIQUE,
    user_id         UUID            NOT NULL,
    risk_score      INT             NOT NULL,
    risk_level      VARCHAR(10)     NOT NULL,
    analysis_notes  TEXT,
    analyzed_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_fraud_analyses_claim_id ON fraud_analyses (claim_id);

-- Notifications table (in notifications schema, but managed by claims-service)
SET search_path TO notifications;

CREATE TABLE IF NOT EXISTS notifications (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID            NOT NULL,
    claim_id    UUID            NOT NULL,
    type        VARCHAR(30)     NOT NULL,
    channel     VARCHAR(10)     NOT NULL,
    message     TEXT            NOT NULL,
    sent_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications (user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_claim_id ON notifications (claim_id);
