# ==============================================================================
# Claims Service - Dockerfile
# Lightweight production image using Eclipse Temurin JRE 17 on Alpine
# ==============================================================================

FROM amazoncorretto:17-alpine

# Set working directory
WORKDIR /app

# Copy the compiled JAR from Maven build output
COPY target/claims-service-*.jar app.jar

# Expose the service port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
