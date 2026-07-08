---
name: service-operations
description: Directives for managing service operations, metrics, and health checks
inclusion: auto
---

# Service Operations Playbook

## Overview

This document defines how to start, stop, and verify all SecureClaims AI microservices locally using Docker Compose.

## Prerequisites

- Docker Desktop must be running
- Java 17 and Maven installed (for building JARs)
- Port 5432 (PostgreSQL), 8081-8084 (services) must be available

## Service Registry

| Service | Port | Health Endpoint | Schema |
|---------|------|----------------|--------|
| PostgreSQL | 5432 | `pg_isready -U admin -d secureclaims` | — |
| identity-service | 8081 | `http://localhost:8081/actuator/health` | identity |
| claims-service | 8082 | `http://localhost:8082/actuator/health` | claims |
| fraud-detection-service | 8083 | `http://localhost:8083/actuator/health` | fraud |
| notification-service | 8084 | `http://localhost:8084/actuator/health` | notifications |

## Startup Sequence

### Step 1: Build All Service JARs

```cmd
mvn clean install -DskipTests
```

### Step 2: Build Docker Images and Start All Services

```cmd
docker compose up -d --build
```

This single command will:
1. Build Docker images for all 4 microservices from their Dockerfiles
2. Start PostgreSQL and wait for it to become healthy
3. Start all microservices once PostgreSQL is ready

### Step 3: Verify All Services Are Running

```cmd
docker compose ps
```

Expected output: All 5 containers should show `Up` status with health state `(healthy)`.

### Step 4: Health Checks

Wait ~30-45 seconds after starting, then verify:

```cmd
curl -s http://localhost:8081/actuator/health
curl -s http://localhost:8082/actuator/health
curl -s http://localhost:8083/actuator/health
curl -s http://localhost:8084/actuator/health
```

Expected response for each: `{"status":"UP"}`

## Shutdown Sequence

### Stop All Services (preserves images and volumes)

```cmd
docker compose down
```

### Stop Without Removing Containers (pause)

```cmd
docker compose stop
```

### Resume Paused Services

```cmd
docker compose start
```

> NEVER use `docker compose down --rmi all` unless you want to re-download images and rebuild.
> NEVER use `docker compose down -v` unless you want to delete all database data.

## Rebuild After Code Changes

After modifying Java source code, rebuild and redeploy:

```cmd
mvn clean install -DskipTests
docker compose up -d --build
```

To rebuild a single service:

```cmd
mvn clean install -DskipTests -pl identity-service
docker compose up -d --build identity-service
```

## Viewing Logs

### All services

```cmd
docker compose logs -f
```

### Single service

```cmd
docker compose logs -f identity-service
```

### Last 100 lines of a service

```cmd
docker compose logs --tail=100 identity-service
```

## Restarting a Single Service

```cmd
docker compose restart identity-service
```

## Troubleshooting

| Issue | Solution |
|-------|---------|
| Port already in use | `netstat -ano \| findstr :8081` then `taskkill /PID <pid> /F`, or stop the conflicting process |
| Database connection refused | Check DB container: `docker compose ps secureclaims-db` |
| Service keeps restarting | Check logs: `docker compose logs identity-service` |
| Health check returns 404 | Verify `management.endpoints.web.exposure.include: health` in application.yml |
| JWT_SECRET error on startup | Ensure the secret is at least 32 characters in docker-compose.yaml |
| Image build fails | Ensure JARs exist: run `mvn clean install -DskipTests` first |
| Container exits immediately | Check logs: `docker compose logs <service-name>` — usually a config or DB issue |
| Stale code running | Force rebuild: `docker compose up -d --build --force-recreate` |

## Environment Variables (configured in docker-compose.yaml)

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `secureclaims-db` | PostgreSQL hostname (Docker service name) |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `secureclaims` | Database name |
| `DB_USERNAME` | `admin` | Database username |
| `DB_PASSWORD` | `admin123` | Database password |
| `JWT_SECRET` | *(required, min 32 chars)* | HMAC-SHA256 signing key |
| `JWT_EXPIRY_MS` | `86400000` | Token expiry (24 hours) |
| `SERVER_PORT` | `8081-8084` | Service-specific port |
