# Health Check All Services

Check the health status of all SecureClaims AI services:

1. Check if PostgreSQL container is running: `docker compose ps`
2. Check identity-service: `curl -s http://localhost:8081/actuator/health`
3. Check claims-service: `curl -s http://localhost:8082/actuator/health`
4. Check fraud-detection-service: `curl -s http://localhost:8083/actuator/health`
5. Check notification-service: `curl -s http://localhost:8084/actuator/health`

Report a summary table showing each service name, port, and status (UP/DOWN/UNREACHABLE).
If any service is down, suggest the fix from the troubleshooting section in .kiro/steering/service-operations.md.
