# SecureClaims AI — AWS Cloud Deployment Guide

## Overview

This guide provides step-by-step instructions to deploy the SecureClaims AI microservices and PostgreSQL database to AWS, optimized for **minimum cost** using free-tier eligible services wherever possible.

---

## Architecture Decision: Cost-Optimized

| Component | Service | Why | Cost |
|-----------|---------|-----|------|
| Container Hosting | **EC2 t2.micro** | Free tier eligible, runs Docker Compose | $0 (12 months free) |
| Container Registry | **GitHub Container Registry (ghcr.io)** | Already configured in CI/CD | $0 |
| Database | **RDS PostgreSQL db.t3.micro** | Managed, free tier eligible | $0 (12 months free) |
| Load Balancer | **None** (use EC2 public IP directly) | ALB costs ~$16/month minimum | $0 |
| Networking | **Default VPC** | Already exists, no cost | $0 |
| Secrets | **Systems Manager Parameter Store** | Free for standard parameters | $0 |
| Monitoring | **CloudWatch** (basic) | Free tier: 10 custom metrics, 5GB logs | $0 |
| CI/CD | **GitHub Actions** (existing) | Already pushes images to ghcr.io | $0 |

### Estimated Monthly Cost

| Scenario | Cost |
|----------|------|
| Within free tier (first 12 months) | **$0** |
| After free tier expires | ~$22/month (EC2 t2.micro + RDS db.t3.micro) |

---

## Recommended Approach: EC2 + RDS + ghcr.io

- **1 EC2 t2.micro** running Docker Compose with all 4 microservices (pulls images from ghcr.io)
- **1 RDS db.t3.micro** for managed PostgreSQL
- **GitHub Container Registry** for image storage (already working)

---

## Deployment Steps

### Phase 1: AWS Account Setup

#### Step 1.1: Verify Free Tier Eligibility

```
AWS Console → Billing → Free Tier Usage
```

Confirm you have free tier remaining for:
- EC2 (750 hours t2.micro/month)
- RDS (750 hours db.t3.micro/month, 20GB storage)

#### Step 1.2: Create IAM User for Deployment

```
AWS Console → IAM → Users → Create User
```

- Username: `secureclaims-deployer`
- Attach policies:
  - `AmazonEC2FullAccess`
  - `AmazonRDSFullAccess`
  - `AmazonSSMReadOnlyAccess`
- Generate Access Keys (for GitHub Actions SSH deploy)

---

### Phase 2: Database Setup (RDS PostgreSQL)

#### Step 2.1: Create RDS Instance

```
AWS Console → RDS → Create Database
```

| Setting | Value |
|---------|-------|
| Engine | PostgreSQL 16 |
| Template | Free tier |
| Instance class | db.t3.micro |
| Storage | 20 GB (General Purpose SSD) |
| DB instance identifier | `secureclaims-db` |
| Master username | `admin` |
| Master password | *(generate and save securely)* |
| Public access | Yes (for initial setup, disable later) |
| VPC | Default VPC |
| Database name | `secureclaims` |
| Backup retention | 1 day (minimum) |
| Auto minor version upgrade | Yes |
| Delete protection | No (for dev) |

#### Step 2.2: Configure Security Group

Allow inbound PostgreSQL (port 5432) from:
- Your EC2 instance's security group
- Your local IP (for initial schema setup only)

#### Step 2.3: Create Schemas

Connect from your local machine:

```bash
psql -h <rds-endpoint>.rds.amazonaws.com -U admin -d secureclaims -f dbScripts/V0__create_database_and_schemas.sql
```

---

### Phase 3: EC2 Instance Setup

#### Step 3.1: Launch EC2 Instance

```
AWS Console → EC2 → Launch Instance
```

| Setting | Value |
|---------|-------|
| Name | `secureclaims-app` |
| AMI | Amazon Linux 2023 |
| Instance type | t2.micro (free tier) or t2.small (if RAM is tight) |
| Key pair | Create new or use existing |
| Security group | Allow SSH (22), and ports 8081-8084 from 0.0.0.0/0 |
| Storage | 20 GB gp3 |

#### Step 3.2: Install Docker on EC2

SSH into the instance:

```bash
ssh -i your-key.pem ec2-user@<public-ip>

# Install Docker
sudo yum update -y
sudo yum install -y docker
sudo service docker start
sudo usermod -a -G docker ec2-user

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Logout and login again for group changes
exit
```

#### Step 3.3: Login to GitHub Container Registry on EC2

```bash
ssh -i your-key.pem ec2-user@<public-ip>

# Login to ghcr.io (use a GitHub PAT with read:packages scope)
echo "<your-github-pat>" | docker login ghcr.io -u meena13oct --password-stdin
```

#### Step 3.4: Create Production Docker Compose

```bash
mkdir ~/secureclaims && cd ~/secureclaims
cat > docker-compose.prod.yaml << 'EOF'
services:
  identity-service:
    image: ghcr.io/meena13oct/secureclaims-ai/identity-service:latest
    restart: on-failure
    ports:
      - "8081:8081"
    environment:
      SERVER_PORT: 8081
      DB_HOST: ${DB_HOST}
      DB_PORT: 5432
      DB_NAME: secureclaims
      DB_USERNAME: admin
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRY_MS: 86400000
    healthcheck:
      test: ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1"]
      interval: 15s
      timeout: 5s
      retries: 5
      start_period: 45s

  claims-service:
    image: ghcr.io/meena13oct/secureclaims-ai/claims-service:latest
    restart: on-failure
    ports:
      - "8082:8082"
    environment:
      SERVER_PORT: 8082
      DB_HOST: ${DB_HOST}
      DB_PORT: 5432
      DB_NAME: secureclaims
      DB_USERNAME: admin
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRY_MS: 86400000
    healthcheck:
      test: ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1"]
      interval: 15s
      timeout: 5s
      retries: 5
      start_period: 45s

  fraud-detection-service:
    image: ghcr.io/meena13oct/secureclaims-ai/fraud-detection-service:latest
    restart: on-failure
    ports:
      - "8083:8083"
    environment:
      SERVER_PORT: 8083
      DB_HOST: ${DB_HOST}
      DB_PORT: 5432
      DB_NAME: secureclaims
      DB_USERNAME: admin
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRY_MS: 86400000
    healthcheck:
      test: ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1"]
      interval: 15s
      timeout: 5s
      retries: 5
      start_period: 45s

  notification-service:
    image: ghcr.io/meena13oct/secureclaims-ai/notification-service:latest
    restart: on-failure
    ports:
      - "8084:8084"
    environment:
      SERVER_PORT: 8084
      DB_HOST: ${DB_HOST}
      DB_PORT: 5432
      DB_NAME: secureclaims
      DB_USERNAME: admin
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRY_MS: 86400000
    healthcheck:
      test: ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1"]
      interval: 15s
      timeout: 5s
      retries: 5
      start_period: 45s
EOF
```

#### Step 3.5: Create Environment File

```bash
cat > .env << 'EOF'
DB_HOST=<your-rds-endpoint>.rds.amazonaws.com
DB_PASSWORD=<your-rds-password>
JWT_SECRET=<production-jwt-secret-at-least-32-chars-long>
EOF
chmod 600 .env
```

#### Step 3.6: Deploy

```bash
docker-compose -f docker-compose.prod.yaml --env-file .env up -d
```

#### Step 3.7: Verify

```bash
docker-compose -f docker-compose.prod.yaml ps
curl -s http://localhost:8081/actuator/health
curl -s http://localhost:8082/actuator/health
curl -s http://localhost:8083/actuator/health
curl -s http://localhost:8084/actuator/health
```

---

### Phase 4: CI/CD Auto-Deploy (Optional)

Add a deploy step to `.github/workflows/ci-cd.yml` that SSHs into EC2 and pulls new images:

#### Step 4.1: Add GitHub Secrets

| Secret | Value |
|--------|-------|
| `EC2_HOST` | EC2 public IP or DNS |
| `EC2_SSH_KEY` | Contents of your .pem file |

#### Step 4.2: Add Deploy Step to Pipeline

```yaml
      - name: Deploy to EC2
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ec2-user
          key: ${{ secrets.EC2_SSH_KEY }}
          script: |
            cd /home/ec2-user/secureclaims
            docker-compose -f docker-compose.prod.yaml pull
            docker-compose -f docker-compose.prod.yaml --env-file .env up -d
```

This gives you full CI/CD: push to main → build → push image to ghcr.io → auto-deploy to EC2.

---

### Phase 5: Security Hardening

#### Step 5.1: Security Groups

| Service | Inbound Rules |
|---------|--------------|
| EC2 (secureclaims-app) | SSH (22) from your IP only; 8081-8084 from 0.0.0.0/0 |
| RDS (secureclaims-db) | PostgreSQL (5432) from EC2 security group only |

#### Step 5.2: Disable RDS Public Access

After initial schema setup:
```
RDS → Modify → Public accessibility: No
```

#### Step 5.3: Use SSM Parameter Store for Secrets (Optional)

```bash
aws ssm put-parameter --name "/secureclaims/db-password" --value "<password>" --type SecureString
aws ssm put-parameter --name "/secureclaims/jwt-secret" --value "<secret>" --type SecureString
```

---

### Phase 6: Verification

#### Step 6.1: Test Health Endpoints

```bash
curl http://<ec2-public-ip>:8081/actuator/health
curl http://<ec2-public-ip>:8082/actuator/health
curl http://<ec2-public-ip>:8083/actuator/health
curl http://<ec2-public-ip>:8084/actuator/health
```

#### Step 6.2: Test Registration & Login

```bash
# Register
curl -X POST http://<ec2-public-ip>:8081/api/identity/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Doe","email":"jane@example.com","username":"janedoe","password":"SecureP@ss1"}'

# Login
curl -X POST http://<ec2-public-ip>:8081/api/identity/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@example.com","password":"SecureP@ss1"}'
```

---

## Quick Reference: Services Used

| Service | Free Tier | Purpose |
|---------|-----------|---------|
| EC2 t2.micro | 750 hrs/month (12 months) | Host Docker containers |
| RDS db.t3.micro | 750 hrs/month (12 months) | Managed PostgreSQL |
| GitHub Container Registry | Free for public repos | Docker image storage |
| SSM Parameter Store | Free (standard) | Store secrets |
| CloudWatch | Basic free | Logs & monitoring |
| Default VPC | Free | Networking |

---

## Alternative: Zero-Cost Option (All on EC2)

If you want absolutely $0 and don't need managed DB:

1. Use your existing `docker-compose.yaml` as-is (includes PostgreSQL container)
2. Copy it to EC2 and run `docker-compose up -d`
3. Trade-off: no managed backups, data lost if instance terminates

---

## Cleanup (When Done)

To avoid ongoing charges:

```bash
# Terminate EC2 instance
aws ec2 terminate-instances --instance-ids <instance-id>

# Delete RDS instance (skip final snapshot for dev)
aws rds delete-db-instance --db-instance-identifier secureclaims-db --skip-final-snapshot
```

---

## Deployment Flow Summary

```
Developer pushes to main
    → GitHub Actions builds & tests
    → Docker images pushed to ghcr.io
    → (Optional) Auto-deploy to EC2 via SSH
    → EC2 pulls latest images from ghcr.io
    → Services connect to RDS PostgreSQL
    → Application live at http://<ec2-ip>:8081-8084
```

---

*Generated: 2026-07-08 | Project: SecureClaims AI | Target: AWS Free Tier*
