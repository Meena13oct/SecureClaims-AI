# Start All Services

Start all SecureClaims AI services using Docker Compose:

1. Verify Docker Desktop is running
2. Build all service JARs: `mvn clean install -DskipTests`
3. Build Docker images and start all containers: `docker compose up -d --build`
4. Verify all containers are running: `docker compose ps`
5. Wait 30-45 seconds, then run health checks on all 4 services:
   - http://localhost:8081/actuator/health (identity-service)
   - http://localhost:8082/actuator/health (claims-service)
   - http://localhost:8083/actuator/health (fraud-detection-service)
   - http://localhost:8084/actuator/health (notification-service)
6. Report which services are UP and which failed

If a service fails to start, check logs with: `docker compose logs <service-name>`

To rebuild only a single service after code changes:
```cmd
mvn clean install -DskipTests -pl <service-name>
docker compose up -d --build <service-name>
```
