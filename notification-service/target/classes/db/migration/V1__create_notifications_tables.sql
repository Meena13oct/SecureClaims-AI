-- ============================================================
-- SecureClaims AI - Notification Service Database Script
-- Schema: notifications
-- Tables: notifications
-- ============================================================

-- Create schema if not exists
CREATE SCHEMA IF NOT EXISTS notifications;

-- Set search path
SET search_path TO notifications;

-- ============================================================
-- Table: notifications
-- Stores all notification records sent to users (email/SMS)
-- ============================================================
CREATE TABLE IF NOT EXISTS notifications (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID            NOT NULL,
    claim_id            UUID,
    notification_type   VARCHAR(50)     NOT NULL,
    channel             VARCHAR(10)     NOT NULL,
    recipient_address   VARCHAR(255)    NOT NULL,
    subject             VARCHAR(255),
    message_body        TEXT            NOT NULL,
    delivery_status     VARCHAR(10)     NOT NULL DEFAULT 'SENT',
    sent_at             TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for admin query: get notifications by user
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications (user_id);

-- Index for claim-related notification lookups
CREATE INDEX IF NOT EXISTS idx_notifications_claim_id ON notifications (claim_id);

-- Index for filtering by type
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications (notification_type);

-- Constraint: channel must be EMAIL or SMS
ALTER TABLE notifications
    ADD CONSTRAINT chk_channel CHECK (channel IN ('EMAIL', 'SMS'));

-- Constraint: delivery_status must be SENT or FAILED
ALTER TABLE notifications
    ADD CONSTRAINT chk_delivery_status CHECK (delivery_status IN ('SENT', 'FAILED'));

-- Constraint: notification_type must be a known value
ALTER TABLE notifications
    ADD CONSTRAINT chk_notification_type CHECK (notification_type IN (
        'CLAIM_RECEIVED',
        'FRAUD_ANALYSIS_DONE',
        'STATUS_UPDATED'
    ));
