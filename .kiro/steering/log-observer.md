# Log Observer – Steering Instructions

## Overview

This document provides detailed guidance for the `log-observer` agent on how to analyze logs from the SecureClaims AI Docker Compose environment, what patterns to look for, and how to format output.

---

## Log Collection Strategy

### Initial Collection (Always Run First)

```bash
# Check service status and health
docker compose ps

# Collect recent logs from ALL services (last 200 lines each)
docker compose logs --tail=200 --timestamps
```

### Targeted Collection (When Investigating Specific Issues)

```bash
# Single service deep dive
docker compose logs --tail=500 --timestamps identity-service
docker compose logs --tail=500 --timestamps claims-service
docker compose logs --tail=500 --timestamps fraud-detection-service
docker compose logs --tail=500 --timestamps notification-service
docker compose logs --tail=500 --timestamps secureclaims-db

# Time-based collection (last 30 minutes)
docker compose logs --since=30m --timestamps

# Follow logs in real-time (use sparingly)
docker compose logs -f --tail=50
```

### Database-Specific Collection

```bash
# PostgreSQL logs for slow queries and errors
docker compose logs --tail=300 --timestamps secureclaims-db

# Check database connectivity from a service
docker compose exec secureclaims-db pg_isready -U postgres
```

---

## Pattern Recognition Guide

### Category 1: Errors and Exceptions

| Pattern | Severity | Meaning |
|---------|----------|---------|
| `java.lang.NullPointerException` | HIGH | Null reference access, missing null checks |
| `java.lang.IllegalStateException` | HIGH | Invalid application state |
| `org.springframework.beans.factory.BeanCreationException` | CRITICAL | Spring context failed to initialize |
| `org.postgresql.util.PSQLException` | HIGH | Database operation failed |
| `java.net.ConnectException: Connection refused` | CRITICAL | Service cannot reach dependency |
| `org.springframework.dao.DataAccessException` | HIGH | Data layer failure |
| `javax.persistence.PersistenceException` | HIGH | JPA/Hibernate failure |
| `org.hibernate.exception.LockAcquisitionException` | MEDIUM | Database deadlock detected |
| `StackOverflowError` | CRITICAL | Infinite recursion |
| `OutOfMemoryError` | CRITICAL | JVM heap exhausted |

### Category 2: Security Anomalies

| Pattern | Severity | Meaning |
|---------|----------|---------|
| `401` response in access logs | MEDIUM (single), HIGH (repeated) | Authentication failure |
| `403` response in access logs | HIGH | Authorization denied |
| `io.jsonwebtoken.ExpiredJwtException` | LOW | Normal token expiry |
| `io.jsonwebtoken.SignatureException` | CRITICAL | Potential token tampering |
| `io.jsonwebtoken.MalformedJwtException` | HIGH | Invalid token format |
| `Bad credentials` | MEDIUM (few), CRITICAL (many) | Login failures, possible brute force |
| `Access Denied` with `@PreAuthorize` | MEDIUM | Role mismatch |
| Multiple failed logins from same IP | CRITICAL | Brute force attack |

### Category 3: Performance Issues

| Pattern | Severity | Meaning |
|---------|----------|---------|
| `HikariPool-1 - Connection is not available` | CRITICAL | Connection pool exhausted |
| `Slow query` or query time > 1000ms | HIGH | Database performance issue |
| `SocketTimeoutException` | HIGH | Network timeout |
| `ReadTimeoutException` | HIGH | Service response too slow |
| `o.s.w.s.m.s.DefaultHandlerExceptionResolver` | MEDIUM | Request processing issues |
| `GC pause` > 500ms | HIGH | Garbage collection pressure |
| `Thread pool exhausted` | CRITICAL | No threads available |
| Response time > 3000ms in access logs | HIGH | Endpoint too slow |

### Category 4: Service Health

| Pattern | Severity | Meaning |
|---------|----------|---------|
| Container status `Exited (137)` | CRITICAL | OOM killed by Docker |
| Container status `Exited (1)` | CRITICAL | Application crash |
| Restart count > 0 | HIGH | Service instability |
| `health: starting` for > 60 seconds | HIGH | Startup stuck |
| `APPLICATION FAILED TO START` | CRITICAL | Spring Boot startup failure |
| `Port already in use` | CRITICAL | Port conflict |
| `Tomcat started on port` not appearing | CRITICAL | Web server never started |

### Category 5: Data Anomalies

| Pattern | Severity | Meaning |
|---------|----------|---------|
| `ConstraintViolationException` | HIGH | Data validation failure at DB level |
| `DataIntegrityViolationException` | HIGH | Referential integrity broken |
| `duplicate key value violates unique constraint` | MEDIUM | Duplicate insert attempted |
| `ERROR: deadlock detected` | HIGH | Concurrent transaction conflict |
| `could not execute statement` | HIGH | SQL execution failure |
| Flyway/Liquibase migration error | CRITICAL | Schema migration failed |

### Category 6: Event Processing

| Pattern | Severity | Meaning |
|---------|----------|---------|
| `Failed to handle event` | HIGH | Event listener exception |
| `EventPublicationRegistry` errors | HIGH | Event publishing failure |
| `@EventListener` + exception in stack trace | HIGH | Event handler crashed |
| `@TransactionalEventListener` + rollback | MEDIUM | Event processed but transaction failed |
| Repeated identical event processing errors | CRITICAL | Poison message scenario |

---

## Correlation Rules

When multiple services show related errors, follow these correlation patterns:

### Authentication Chain
```
identity-service JWT error -> claims-service 401 -> fraud-detection-service 401
```
Root cause: identity-service token generation or validation issue.

### Database Cascade
```
secureclaims-db connection limit -> identity-service pool exhaustion -> claims-service pool exhaustion
```
Root cause: PostgreSQL max_connections reached or connection leak.

### Event Chain
```
claims-service publishes event -> fraud-detection-service fails to process -> notification-service never triggered
```
Root cause: Event handler exception in fraud-detection-service.

### Startup Order
```
claims-service fails -> depends on secureclaims-db not ready
```
Root cause: Database not accepting connections when service starts.

---

## Fix Suggestion Templates

### Connection Pool Exhaustion
```yaml
# File: <service>/src/main/resources/application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

### JWT Configuration Tuning
```yaml
# File: identity-service/src/main/resources/application.yml
jwt:
  expiration: 86400000
  refresh-expiration: 604800000
```

### Slow Query Optimization
```
Add database indexes on frequently queried columns.
Review N+1 query patterns in repository methods.
Use @EntityGraph or JOIN FETCH for eager loading where needed.
Consider pagination for large result sets.
```

### Event Handler Error Recovery
```
Wrap event listener logic in try-catch with proper logging.
Store failed events for retry processing.
Add @Retryable annotation for transient failures.
Implement dead letter queue pattern for persistent failures.
```

### OOM Prevention
```dockerfile
# File: <service>/Dockerfile
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

---

## Output Formatting Rules

1. **Always start** with the summary report header showing timestamp and services analyzed.
2. **Group findings** by severity (CRITICAL first, then HIGH, MEDIUM, LOW).
3. **Within each severity**, group by service name.
4. **Include log evidence** by quoting the actual log line(s) that triggered the finding.
5. **Be precise in fix suggestions** and include the exact file path relative to workspace root.
6. **End with statistics** showing total counts per severity and per category.
7. **If no anomalies found**, report a clean health status with confidence level.

### Clean Report Format
```
All services are running normally. No errors, warnings, or
performance issues detected in the last 200 log entries.

Service Uptime:
  identity-service:         Up (healthy)
  claims-service:           Up (healthy)
  fraud-detection-service:  Up (healthy)
  notification-service:     Up (healthy)
  secureclaims-db:          Up (healthy)
```

---

## Escalation Rules

- **CRITICAL findings**: Always recommend immediate action. Suggest specific commands to recover.
- **HIGH findings**: Recommend investigation within current session. Provide fix.
- **MEDIUM findings**: Note for next maintenance window. Provide fix suggestion.
- **LOW findings**: Informational only. Group at end of report.

---

## Service-Specific Log Locations (In Container)

| Service | Log Path | Framework |
|---------|----------|-----------|
| identity-service | stdout (Docker) | SLF4J + Logback |
| claims-service | stdout (Docker) | SLF4J + Logback |
| fraud-detection-service | stdout (Docker) | SLF4J + Logback |
| notification-service | stdout (Docker) | SLF4J + Logback |
| secureclaims-db | /var/log/postgresql/ | PostgreSQL native |

All Spring Boot services log to stdout by default, which Docker captures. Use `docker compose logs` to access them.
