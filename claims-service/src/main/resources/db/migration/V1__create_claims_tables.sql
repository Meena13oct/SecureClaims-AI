-- ============================================================
-- SecureClaims AI - Claims Service Database Script
-- Schema: claims
-- Tables: claims, documents
-- ============================================================

-- Create schema if not exists
CREATE SCHEMA IF NOT EXISTS claims;

-- Set search path
SET search_path TO claims;

-- ============================================================
-- Table: claims
-- Stores insurance claim submissions and their lifecycle status
-- ============================================================
CREATE TABLE IF NOT EXISTS claims (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL,
    policy_number   VARCHAR(50)     NOT NULL,
    claim_type      VARCHAR(50)     NOT NULL,
    incident_date   DATE            NOT NULL,
    description     TEXT            NOT NULL,
    claimed_amount  DECIMAL(15, 2)  NOT NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'SUBMITTED',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for user's claims retrieval
CREATE INDEX IF NOT EXISTS idx_claims_user_id ON claims (user_id);

-- Index for filtering by status (admin queries)
CREATE INDEX IF NOT EXISTS idx_claims_status ON claims (status);

-- Index for combined user + status query
CREATE INDEX IF NOT EXISTS idx_claims_user_status ON claims (user_id, status);

-- ============================================================
-- Table: documents
-- Stores metadata for uploaded claim documents (files on local FS)
-- ============================================================
CREATE TABLE IF NOT EXISTS documents (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    claim_id            UUID            NOT NULL,
    original_filename   VARCHAR(255)    NOT NULL,
    stored_filename     VARCHAR(255)    NOT NULL,
    file_path           VARCHAR(500)    NOT NULL,
    mime_type           VARCHAR(100)    NOT NULL,
    file_size_bytes     BIGINT          NOT NULL,
    uploaded_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_documents_claim FOREIGN KEY (claim_id) REFERENCES claims (id) ON DELETE CASCADE
);

-- Index for retrieving documents by claim
CREATE INDEX IF NOT EXISTS idx_documents_claim_id ON documents (claim_id);
