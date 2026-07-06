# Objective
Follow the instructions below to generate a production-ready CI/CD configuration.

# Instructions
Create a production-ready GitHub Actions CI/CD pipeline for a Spring Boot application that uses PostgreSQL (running in Docker).

## Requirements:
1. CI Pipeline (on every push and pull request):
- Checkout code
- Cache Maven dependencies
- Run unit tests
- Build all modules using root pom.xml (mvn clean install)
- Ensure application compiles successfully

## 2. Database Integration for Tests:
- Start PostgreSQL service using GitHub Actions services
- Use environment variables:
  - POSTGRES_DB=secureclaims
  - POSTGRES_USER=admin
  - POSTGRES_PASSWORD=admin123
- Expose port 5432
- Add healthcheck to PostgreSQL service (`pg_isready`)
Ensure Spring Boot tests connect to this DB using `application-test.properties` or environment variables

## 3. Flyway Support:
- Ensure Flyway migrations run during pipeline execution
- Set `spring.flyway.enabled=true` in test profile

## 4. Microservices Docker Build:
Build Docker images separately for:
claims-service
fraud-detection-service
identity-service
notification-service
Tag each image with service name and commit SHA

## 5. CD Pipeline (on main branch only):
- Build Docker image for Spring Boot app
- Tag image with commit SHA
- (Optional placeholder) push image GitHub Container Registry

## 6. Best Practices:
- Use separate jobs for build and deploy
- Use environment variables securely
- Add clear comments in YAML
- Add artifact retention policy (7 days)Use 
- Optimize caching for Maven

### Output
- Provide full `./SecureClaims-AI/.github/workflows/ci-cd.yml`
- Keep it clean, modular, and production-ready
- Ensure it supports multi-module microservices architecture
- Include comments explaining each section
