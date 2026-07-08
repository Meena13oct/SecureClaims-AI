# Create a Docker Compose configuration for a PostgreSQL database.

## Requirements:
Use the latest official PostgreSQL image
Container name should be "secureclaims-db"
Expose port 5432
Set environment variables:
POSTGRES_USER = admin
POSTGRES_PASSWORD = admin123
POSTGRES_DB = secureclaims
Add a named volume for persistent storage
Ensure the container restarts automatically
PostgreSQL Version Control Requirement
Use the official PostgreSQL Docker image
The image must be strictly version-pinned (e.g., postgres:16.3)
Do NOT use the latest tag
The pinned version must not change automatically
Optional (High-Security Environments):
For complete immutability, use a Docker image digest: postgres@sha256:<digest>
This ensures the image is fully locked and reproducible
Version Upgrade Policy:
PostgreSQL version upgrades must be done manually and intentionally
Any upgrade should go through testing before deployment
Keep the configuration clean and production-ready