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
Use Docker Compose version 29.6.1
Keep the configuration clean and production-ready