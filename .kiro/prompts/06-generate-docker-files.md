# Role
You are an expert DevOps engineer specializing in Docker containerization and Java Spring Boot multi-module architectures.

# Objective
Generate optimized, production-ready, individual `Dockerfile` configurations for each of the microservices in this repository.

# Microservices List
Create a separate `Dockerfile` at the root of each of these specific directories:
1. `claims-service/`
2. `fraud-detection-service/`
3. `identity-service/`
4. `notification-service/`

# Dockerfile Structural Requirements
For each service, the `Dockerfile` must adhere to these standards:
*   **Base Image:** Use a lightweight, secure base image suited for Java 17 runtime (e.g., `amazoncorretto:17-alpine`).
*   **Working Directory:** Set the container workspace to `/app`.
*   **Artifact Copying:** Copy the compiled heavy JAR file from the local module's `target/` directory into the container workspace as `app.jar`.
*   **Dynamic Matching:** Account for standard Maven snapshot naming conventions (e.g., matching `target/*-SNAPSHOT.jar` or dynamically targeting the specific module name).
*   **Networking:** Expose a standard port placeholder configuration (e.g., `EXPOSE 8080` or individual microservice ports if specified in the module configurations).
*   **Execution:** Set the default `ENTRYPOINT` executing `java -jar app.jar`.

# Action Items
1. Scan the respective target folders to verify build artifact names if available.
2. Generate the lightweight `Dockerfile` text content.
3. Write each configuration file directly to its corresponding folder (e.g., `claims-service/Dockerfile`).
