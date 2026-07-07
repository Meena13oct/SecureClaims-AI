-- ============================================================
-- SecureClaims AI - Seed Data for US-001
-- Purpose: Initialize default roles for the identity schema
-- Run AFTER schema creation (V0) and table migrations (V1)
-- ============================================================

-- Connect to secureclaims database before running

SET search_path TO identity;

-- ============================================================
-- Default Roles
-- These roles are referenced by JWT tokens: ROLE_USER, ROLE_ADMIN
-- ============================================================
INSERT INTO roles (id, name) VALUES
    (gen_random_uuid(), 'USER'),
    (gen_random_uuid(), 'ADMIN')
ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- Verification query
-- ============================================================
SELECT id, name FROM identity.roles;
