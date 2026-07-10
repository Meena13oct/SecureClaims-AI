-- ============================================================
-- SecureClaims AI - Master Database Setup Script
-- Run this ONCE to create the database and all schemas
-- PostgreSQL: connect as superuser (postgres)
-- ============================================================

-- Create the database (run manually if not exists)
-- CREATE DATABASE secureclaims;

-- Connect to secureclaims database before running the lines below

-- ============================================================
-- Create all schemas
-- ============================================================
CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS claims;
CREATE SCHEMA IF NOT EXISTS fraud;
CREATE SCHEMA IF NOT EXISTS notifications;

-- ============================================================
-- Grant permissions to the application user
-- ============================================================
GRANT ALL PRIVILEGES ON SCHEMA identity TO CURRENT_USER;
GRANT ALL PRIVILEGES ON SCHEMA claims TO CURRENT_USER;
GRANT ALL PRIVILEGES ON SCHEMA fraud TO CURRENT_USER;
GRANT ALL PRIVILEGES ON SCHEMA notifications TO CURRENT_USER;

-- ============================================================
-- Verify schemas created
-- ============================================================
SELECT schema_name FROM information_schema.schemata
WHERE schema_name IN ('identity', 'claims', 'fraud', 'notifications');
