-- ============================================================
-- SecureClaims AI - Identity Service Database Script
-- Schema: identity
-- Tables: users, roles, user_roles
-- ============================================================

-- Create schema if not exists
CREATE SCHEMA IF NOT EXISTS identity;

-- Set search path
SET search_path TO identity;

-- ============================================================
-- Table: users
-- Stores registered user accounts
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    username        VARCHAR(50)     NOT NULL UNIQUE,
    email           VARCHAR(100)    NOT NULL UNIQUE,
    first_name      VARCHAR(50)     NOT NULL,
    last_name       VARCHAR(50)     NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for login lookup by email
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

-- Index for username lookup
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);

-- ============================================================
-- Table: roles
-- Stores available roles (USER, ADMIN)
-- ============================================================
CREATE TABLE IF NOT EXISTS roles (
    id      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name    VARCHAR(20)     NOT NULL UNIQUE
);

-- ============================================================
-- Table: user_roles (Join Table)
-- Maps users to their assigned roles (many-to-many)
-- ============================================================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id     UUID    NOT NULL,
    role_id     UUID    NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Index for user role lookups
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles (user_id);

-- ============================================================
-- Seed Data: Default roles
-- ============================================================
INSERT INTO roles (id, name) VALUES
    (gen_random_uuid(), 'USER'),
    (gen_random_uuid(), 'ADMIN')
ON CONFLICT (name) DO NOTHING;
