# Stop All Services

Gracefully stop all SecureClaims AI services using Docker Compose:

## Stop and Remove Containers (preserves images and volumes)

```cmd
docker compose down
```

## Stop Without Removing Containers (pause)

```cmd
docker compose stop
```

## Resume Paused Services

```cmd
docker compose start
```

## Verify All Services Are Stopped

```cmd
docker compose ps
```

Expected output: No containers listed, or all containers show `Exited` status.

## Safety Rules

- Do NOT use `docker compose down --rmi all` unless you want to re-download images and rebuild.
- Do NOT use `docker compose down -v` unless you want to delete all database data.
