# SecureClaims AI — AWS CloudFormation Deployment Guide

## Document Information

| Field | Value |
|-------|-------|
| **Project** | SecureClaims AI – Insurance Claims Processing System |
| **Purpose** | Step-by-step guide to deploy infrastructure using AWS CloudFormation |
| **Date** | 2026-07-08 |
| **Template Location** | `infrastructure/cloudformation/secureclaims-stack.yaml` |
| **Cost Target** | $0/month (AWS Free Tier eligible) |

---

## Table of Contents

| #  | Section |
|----|---------|
| 1  | Architecture Overview |
|    | *What CloudFormation creates, architecture diagram, network design* |
| 2  | Prerequisites |
|    | *AWS account, key pair, CLI setup, local tools needed* |
| 3  | CloudFormation Template Parameters |
|    | *All input parameters with types and defaults* |
| 4  | Template File Location and Structure |
|    | *Where the template lives in the project, YAML structure* |
| 5  | How to Execute the CloudFormation Template |
|    | *3 options: AWS CLI, Console UI, GitHub Actions* |
| 6  | Stack Lifecycle Management |
|    | *Update, delete, and snapshot commands* |
| 7  | EC2 Bootstrap Process (UserData) |
|    | *What happens when EC2 launches — Docker install, image pull, startup* |
| 8  | Post-Deployment Verification |
|    | *Health checks, end-to-end API testing, SSH debugging* |
| 9  | Troubleshooting |
|    | *Common errors during stack creation and service startup* |
| 10 | Cost Breakdown |
|    | *Free tier usage, post-free-tier costs, optimization tips* |
| 11 | Checking Into GitHub |
|    | *What to commit, what NOT to commit, push commands* |
| 12 | CI/CD Integration |
|    | *Full pipeline flow, required GitHub secrets* |
| 13 | Quick Reference Commands |
|    | *All commands in one table for fast lookup* |
| 14 | Next Steps |
|    | *Actions after this guide is reviewed* |

---

> 🔷 = Infrastructure & Technical Sections  |  🔶 = Process & Operational Sections

---

## 🔷 1. Architecture Overview

### 1.1 What CloudFormation Will Create

The CloudFormation template provisions the following AWS resources as a single stack:

| Resource | AWS Service | Specification | Purpose |
|----------|-------------|---------------|---------|
| VPC | Amazon VPC | 10.0.0.0/16, 2 public subnets | Network isolation |
| Internet Gateway | VPC IGW | Attached to VPC | Internet access for EC2 |
| EC2 Instance | Amazon EC2 | t2.micro, Amazon Linux 2023 | Host all 4 Docker containers |
| Security Group (EC2) | EC2 SG | Ports: 22, 8081-8084 | Control inbound traffic |
| Security Group (RDS) | RDS SG | Port: 5432 from EC2 SG only | Database access control |
| RDS Instance | Amazon RDS | db.t3.micro, PostgreSQL 16, 20GB | Managed database |
| DB Subnet Group | RDS Subnet Group | 2 subnets | RDS placement |
| SSM Parameters | AWS SSM | SecureString | Store DB password, JWT secret |
| IAM Instance Profile | IAM | SSM read access | EC2 reads secrets from SSM |

### 1.2 Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         AWS Cloud (VPC)                          │
│                         10.0.0.0/16                              │
│                                                                 │
│  ┌──────────────────────────┐   ┌──────────────────────────┐   │
│  │   Public Subnet A         │   │   Public Subnet B         │   │
│  │   10.0.1.0/24            │   │   10.0.2.0/24            │   │
│  │                          │   │                          │   │
│  │  ┌────────────────────┐  │   │                          │   │
│  │  │  EC2 t2.micro      │  │   │                          │   │
│  │  │  (Docker Compose)  │  │   │                          │   │
│  │  │                    │  │   │                          │   │
│  │  │  • identity:8081   │  │   │  ┌────────────────────┐  │   │
│  │  │  • claims:8082     │──│───│──│  RDS db.t3.micro   │  │   │
│  │  │  • fraud:8083      │  │   │  │  PostgreSQL 16     │  │   │
│  │  │  • notify:8084     │  │   │  │  secureclaims DB   │  │   │
│  │  └────────────────────┘  │   │  └────────────────────┘  │   │
│  └──────────────────────────┘   └──────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                          │
                ┌─────────▼─────────┐
                │  Internet Gateway  │
                └─────────┬─────────┘
                          │
                ┌─────────▼─────────┐
                │     Internet       │
                │  (Users/Postman)   │
                └───────────────────┘
```

### 1.3 Network Design

| Subnet | CIDR | AZ | Purpose |
|--------|------|-----|---------|
| Public Subnet A | 10.0.1.0/24 | ap-south-1a | EC2 instance |
| Public Subnet B | 10.0.2.0/24 | ap-south-1b | RDS (requires 2 AZs for subnet group) |

### 1.4 Security Groups

| Security Group | Inbound Rules | Purpose |
|----------------|---------------|---------|
| EC2-SG | SSH (22) from your IP; 8081-8084 from 0.0.0.0/0 | App access |
| RDS-SG | PostgreSQL (5432) from EC2-SG only | DB access restricted to EC2 |

---

## 🔶 2. Prerequisites

### 2.1 AWS Account Requirements

| Requirement | Details |
|-------------|---------|
| AWS Account | Active account with free tier eligibility |
| IAM User | Admin access or CloudFormation + EC2 + RDS + SSM + IAM permissions |
| AWS CLI | Installed and configured (`aws configure`) |
| Region | `ap-south-1` (Mumbai) |
| EC2 Key Pair | Must exist in the target region before stack creation |

### 2.2 Create EC2 Key Pair (if not exists)

```bash
# Create a new key pair and save the .pem file
aws ec2 create-key-pair \
  --key-name secureclaims-keypair \
  --query 'KeyMaterial' \
  --output text \
  --region ap-south-1 > secureclaims-keypair.pem

# Set correct permissions (Linux/Mac)
chmod 400 secureclaims-keypair.pem

# On Windows PowerShell
# File is saved as secureclaims-keypair.pem in current directory
```

### 2.3 Get Your Public IP (for SSH access)

```bash
curl -s https://checkip.amazonaws.com
```

Use this IP in the `SSHLocation` parameter when creating the stack.

### 2.4 Local Requirements

| Tool | Version | Purpose |
|------|---------|---------|
| AWS CLI | v2.x | Create/manage CloudFormation stacks |
| Docker | Latest | Build images locally (already done) |
| Git | Latest | Push template to GitHub |

---

## 🔷 3. CloudFormation Template Parameters

When creating the stack, you'll provide these parameters:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `EnvironmentName` | String | `secureclaims` | Prefix for all resource names |
| `VpcCIDR` | String | `10.0.0.0/16` | VPC CIDR block |
| `PublicSubnet1CIDR` | String | `10.0.1.0/24` | Subnet A CIDR |
| `PublicSubnet2CIDR` | String | `10.0.2.0/24` | Subnet B CIDR |
| `KeyPairName` | AWS::EC2::KeyPair::KeyName | *(required)* | EC2 SSH key pair name |
| `SSHLocation` | String | `0.0.0.0/0` | IP range allowed for SSH |
| `InstanceType` | String | `t2.micro` | EC2 instance type |
| `DBInstanceClass` | String | `db.t3.micro` | RDS instance class |
| `DBName` | String | `secureclaims` | Database name |
| `DBUsername` | String | `admin` | Database master username |
| `DBPassword` | String | *(required, NoEcho)* | Database master password (min 8 chars) |
| `JWTSecret` | String | *(required, NoEcho)* | JWT signing secret (min 32 chars) |
| `GitHubUsername` | String | `meena13oct` | GitHub username for ghcr.io login |
| `GitHubPAT` | String | *(required, NoEcho)* | GitHub PAT with `read:packages` scope |

---

## 🔶 4. Template File Location and Structure

### 4.1 File Path

```
SecureClaims-AI/
├── infrastructure/
│   └── cloudformation/
│       ├── secureclaims-stack.yaml      ← Main CloudFormation template
│       └── README.md                     ← Quick-start instructions
├── .kiro/
│   └── outputs/
│       └── aws-cloudformation-guide.md   ← This document
└── ...
```

### 4.2 Template Structure

```yaml
AWSTemplateFormatVersion: '2010-09-09'
Description: SecureClaims AI Infrastructure Stack

Parameters:         # Input parameters (DB password, key pair, etc.)
Resources:          # AWS resources to create
  # Networking
  - VPC, Subnets, Internet Gateway, Route Tables
  # Security
  - EC2 Security Group, RDS Security Group
  - IAM Role and Instance Profile
  # Database
  - DB Subnet Group, RDS Instance
  # Compute
  - EC2 Instance (with UserData bootstrap script)
Outputs:            # Stack outputs (EC2 IP, RDS endpoint, etc.)
```



---

## 🔷 5. How to Execute the CloudFormation Template

### 5.1 Option A: AWS CLI (Recommended)

#### Step 1: Validate the Template

```bash
aws cloudformation validate-template \
  --template-body file://infrastructure/cloudformation/secureclaims-stack.yaml \
  --region ap-south-1
```

Expected output: JSON with parameters and description (no errors).

#### Step 2: Create the Stack

```bash
aws cloudformation create-stack \
  --stack-name secureclaims-ai \
  --template-body file://infrastructure/cloudformation/secureclaims-stack.yaml \
  --parameters \
    ParameterKey=KeyPairName,ParameterValue=secureclaims-keypair \
    ParameterKey=SSHLocation,ParameterValue=<your-ip>/32 \
    ParameterKey=DBPassword,ParameterValue=<your-db-password> \
    ParameterKey=JWTSecret,ParameterValue=<your-jwt-secret-min-32-chars> \
    ParameterKey=GitHubUsername,ParameterValue=meena13oct \
    ParameterKey=GitHubPAT,ParameterValue=<your-github-pat> \
  --capabilities CAPABILITY_IAM \
  --region ap-south-1
```

#### Step 3: Monitor Stack Creation

```bash
# Watch stack events in real-time
aws cloudformation describe-stack-events \
  --stack-name secureclaims-ai \
  --region ap-south-1 \
  --query 'StackEvents[*].[Timestamp,ResourceType,LogicalResourceId,ResourceStatus]' \
  --output table

# Or wait until complete
aws cloudformation wait stack-create-complete \
  --stack-name secureclaims-ai \
  --region ap-south-1
```

Expected duration: **5-10 minutes** (RDS creation takes the longest).

#### Step 4: Get Stack Outputs

```bash
aws cloudformation describe-stacks \
  --stack-name secureclaims-ai \
  --region ap-south-1 \
  --query 'Stacks[0].Outputs' \
  --output table
```

This returns:
- EC2 public IP address
- RDS endpoint
- Service health check URLs

#### Step 5: Verify Deployment

```bash
# SSH into EC2 to check containers
ssh -i secureclaims-keypair.pem ec2-user@<ec2-public-ip>
docker ps

# Test health endpoints from your local machine
curl http://<ec2-public-ip>:8081/actuator/health
curl http://<ec2-public-ip>:8082/actuator/health
curl http://<ec2-public-ip>:8083/actuator/health
curl http://<ec2-public-ip>:8084/actuator/health
```

---

### 5.2 Option B: AWS Console (UI)

1. Open **AWS Console → CloudFormation → Create Stack**
2. Select **"Upload a template file"**
3. Upload `infrastructure/cloudformation/secureclaims-stack.yaml`
4. Click **Next**
5. Fill in stack name: `secureclaims-ai`
6. Fill in all parameters (DB password, JWT secret, key pair name, GitHub PAT)
7. Click **Next** → **Next** → Check "I acknowledge that AWS CloudFormation might create IAM resources"
8. Click **Create Stack**
9. Wait for status: `CREATE_COMPLETE` (~5-10 minutes)
10. Go to **Outputs** tab to get EC2 IP and RDS endpoint

---

### 5.3 Option C: GitHub Actions (Automated)

Add a deploy job to `.github/workflows/ci-cd.yml`:

```yaml
  deploy-infrastructure:
    name: Deploy CloudFormation Stack
    runs-on: ubuntu-latest
    needs: build
    if: github.ref == 'refs/heads/main' && github.event_name == 'workflow_dispatch'
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: us-east-1

      - name: Deploy CloudFormation stack
        run: |
          aws cloudformation deploy \
            --stack-name secureclaims-ai \
            --template-file infrastructure/cloudformation/secureclaims-stack.yaml \
            --parameter-overrides \
              KeyPairName=${{ secrets.EC2_KEY_PAIR_NAME }} \
              DBPassword=${{ secrets.DB_PASSWORD }} \
              JWTSecret=${{ secrets.JWT_SECRET }} \
              GitHubUsername=${{ github.actor }} \
              GitHubPAT=${{ secrets.GH_PAT_PACKAGES }} \
            --capabilities CAPABILITY_IAM \
            --no-fail-on-empty-changeset
```

---

## 🔶 6. Stack Lifecycle Management

### 6.1 Update the Stack

After modifying the template:

```bash
aws cloudformation update-stack \
  --stack-name secureclaims-ai \
  --template-body file://infrastructure/cloudformation/secureclaims-stack.yaml \
  --parameters \
    ParameterKey=KeyPairName,UsePreviousValue=true \
    ParameterKey=SSHLocation,UsePreviousValue=true \
    ParameterKey=DBPassword,UsePreviousValue=true \
    ParameterKey=JWTSecret,UsePreviousValue=true \
    ParameterKey=GitHubUsername,UsePreviousValue=true \
    ParameterKey=GitHubPAT,UsePreviousValue=true \
  --capabilities CAPABILITY_IAM \
  --region ap-south-1
```

### 6.2 Delete the Stack (Cleanup)

```bash
aws cloudformation delete-stack \
  --stack-name secureclaims-ai \
  --region ap-south-1

# Wait for deletion
aws cloudformation wait stack-delete-complete \
  --stack-name secureclaims-ai \
  --region ap-south-1
```

> **Warning:** This deletes ALL resources (EC2, RDS, VPC, etc.). Data in RDS will be lost unless you take a snapshot first.

### 6.3 Take RDS Snapshot Before Deletion

```bash
aws rds create-db-snapshot \
  --db-instance-identifier secureclaims-db \
  --db-snapshot-identifier secureclaims-backup-$(date +%Y%m%d) \
  --region ap-south-1
```



---

## 🔷 7. EC2 Bootstrap Process (UserData)

The CloudFormation template includes a `UserData` script that automatically configures the EC2 instance on first boot:

### 7.1 What the Bootstrap Script Does

```
EC2 Instance Launches
    │
    ▼
[1] Install Docker & Docker Compose
    │
    ▼
[2] Login to GitHub Container Registry (ghcr.io)
    │
    ▼
[3] Create docker-compose.prod.yaml with RDS endpoint
    │
    ▼
[4] Create .env file with secrets from parameters
    │
    ▼
[5] Pull images from ghcr.io
    │
    ▼
[6] Run docker-compose up -d
    │
    ▼
[7] Initialize database schemas via psql
    │
    ▼
[8] Services are live and healthy
```

### 7.2 Bootstrap Script (Embedded in Template)

```bash
#!/bin/bash -xe
# Update system
yum update -y

# Install Docker
yum install -y docker
systemctl enable docker
systemctl start docker
usermod -a -G docker ec2-user

# Install Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" \
  -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Install PostgreSQL client (for schema initialization)
yum install -y postgresql16

# Login to GitHub Container Registry
echo "${GitHubPAT}" | docker login ghcr.io -u ${GitHubUsername} --password-stdin

# Create application directory
mkdir -p /home/ec2-user/secureclaims
cd /home/ec2-user/secureclaims

# Create docker-compose.prod.yaml
cat > docker-compose.prod.yaml << 'COMPOSE'
services:
  identity-service:
    image: ghcr.io/meena13oct/secureclaims-ai/identity-service:latest
    restart: always
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

  claims-service:
    image: ghcr.io/meena13oct/secureclaims-ai/claims-service:latest
    restart: always
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

  fraud-detection-service:
    image: ghcr.io/meena13oct/secureclaims-ai/fraud-detection-service:latest
    restart: always
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

  notification-service:
    image: ghcr.io/meena13oct/secureclaims-ai/notification-service:latest
    restart: always
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
COMPOSE

# Create .env file
cat > .env << ENV
DB_HOST=<RDS_ENDPOINT>
DB_PASSWORD=<DB_PASSWORD>
JWT_SECRET=<JWT_SECRET>
ENV
chmod 600 .env

# Pull and start services
docker-compose -f docker-compose.prod.yaml --env-file .env up -d

# Wait for RDS to be accessible, then initialize schemas
sleep 10
PGPASSWORD=<DB_PASSWORD> psql -h <RDS_ENDPOINT> -U admin -d secureclaims << 'SQL'
CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS claims;
CREATE SCHEMA IF NOT EXISTS fraud;
CREATE SCHEMA IF NOT EXISTS notifications;
SQL

# Set ownership
chown -R ec2-user:ec2-user /home/ec2-user/secureclaims
```

### 7.3 Estimated Boot Time

| Phase | Duration |
|-------|----------|
| EC2 instance launch | ~30 seconds |
| Package updates & Docker install | ~60 seconds |
| Image pull from ghcr.io (4 images × ~200MB) | ~90 seconds |
| Container startup (Spring Boot) | ~45 seconds |
| **Total time to healthy** | **~4-5 minutes** |

---

## 🔶 8. Post-Deployment Verification

### 8.1 Verify Stack Status

```bash
aws cloudformation describe-stacks \
  --stack-name secureclaims-ai \
  --region ap-south-1 \
  --query 'Stacks[0].StackStatus'
```

Expected: `"CREATE_COMPLETE"`

### 8.2 Get EC2 Public IP

```bash
aws cloudformation describe-stacks \
  --stack-name secureclaims-ai \
  --region ap-south-1 \
  --query 'Stacks[0].Outputs[?OutputKey==`EC2PublicIP`].OutputValue' \
  --output text
```

### 8.3 Test All Service Health Endpoints

```bash
EC2_IP=$(aws cloudformation describe-stacks \
  --stack-name secureclaims-ai \
  --region ap-south-1 \
  --query 'Stacks[0].Outputs[?OutputKey==`EC2PublicIP`].OutputValue' \
  --output text)

echo "Testing identity-service..."
curl -s http://$EC2_IP:8081/actuator/health

echo "Testing claims-service..."
curl -s http://$EC2_IP:8082/actuator/health

echo "Testing fraud-detection-service..."
curl -s http://$EC2_IP:8083/actuator/health

echo "Testing notification-service..."
curl -s http://$EC2_IP:8084/actuator/health
```

Expected response for each: `{"status":"UP"}`

### 8.4 Test End-to-End Flow

```bash
# 1. Register a user
curl -X POST http://$EC2_IP:8081/api/identity/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "jane.doe@example.com",
    "username": "janedoe",
    "password": "SecureP@ss1"
  }'

# 2. Login and get JWT
TOKEN=$(curl -s -X POST http://$EC2_IP:8081/api/identity/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane.doe@example.com",
    "password": "SecureP@ss1"
  }' | jq -r '.data.token')

echo "JWT Token: $TOKEN"

# 3. Use token on other services
curl -H "Authorization: Bearer $TOKEN" http://$EC2_IP:8082/api/claims/v1/claims
```

### 8.5 SSH Into EC2 for Debugging

```bash
ssh -i secureclaims-keypair.pem ec2-user@$EC2_IP

# Check running containers
docker ps

# View logs for a specific service
docker logs identity-service --tail 50

# Check all service logs
docker-compose -f /home/ec2-user/secureclaims/docker-compose.prod.yaml logs --tail 20
```

---

## 🔷 9. Troubleshooting

### 9.1 Stack Creation Failures

| Error | Cause | Solution |
|-------|-------|----------|
| `CREATE_FAILED - EC2 Instance` | Key pair doesn't exist in region | Create key pair first: `aws ec2 create-key-pair` |
| `CREATE_FAILED - RDS Instance` | db.t3.micro not available in AZ | Change to a different AZ or instance class |
| `CREATE_FAILED - Security Group` | VPC limit reached | Delete unused VPCs or request limit increase |
| `ROLLBACK_IN_PROGRESS` | Any resource failed | Check Events tab for root cause, fix, and retry |

### 9.2 Services Not Starting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `docker ps` shows no containers | Bootstrap script failed | Check `/var/log/cloud-init-output.log` |
| Container exits immediately | DB not reachable | Verify RDS security group allows EC2 |
| Health returns 503 | Spring Boot still starting | Wait 60s after container start |
| "Connection refused" on port 8081 | Docker not running | `sudo systemctl start docker` |
| ghcr.io login failed | Invalid PAT | Regenerate PAT with `read:packages` scope |

### 9.3 Database Connection Issues

```bash
# Test DB connectivity from EC2
psql -h <rds-endpoint> -U admin -d secureclaims -c "SELECT 1;"

# If connection times out: RDS security group doesn't allow EC2
# If authentication fails: wrong password in .env file
```

### 9.4 View CloudFormation Events (for debugging)

```bash
aws cloudformation describe-stack-events \
  --stack-name secureclaims-ai \
  --region ap-south-1 \
  --query 'StackEvents[?ResourceStatus==`CREATE_FAILED`].[LogicalResourceId,ResourceStatusReason]' \
  --output table
```

### 9.5 Force Redeploy Containers on EC2

```bash
ssh -i secureclaims-keypair.pem ec2-user@<ec2-ip>
cd /home/ec2-user/secureclaims
docker-compose -f docker-compose.prod.yaml pull
docker-compose -f docker-compose.prod.yaml --env-file .env up -d --force-recreate
```



---

## 🔶 10. Cost Breakdown

### 10.1 Free Tier Eligible Resources

| Resource | Free Tier Allowance | Our Usage | Cost |
|----------|--------------------:|-----------|------|
| EC2 t2.micro | 750 hrs/month (12 months) | 1 instance, 24/7 = 730 hrs | **$0** |
| RDS db.t3.micro | 750 hrs/month (12 months) | 1 instance, 24/7 = 730 hrs | **$0** |
| RDS Storage | 20 GB/month (12 months) | 20 GB gp2 | **$0** |
| Data Transfer | 1 GB/month out | Minimal API traffic | **$0** |
| CloudFormation | Always free | Stack management | **$0** |
| SSM Parameter Store | Free (standard params) | 3-5 parameters | **$0** |

### 10.2 Total Cost

| Scenario | Monthly Cost |
|----------|-------------|
| Within free tier (first 12 months) | **$0.00** |
| After free tier (EC2 + RDS) | ~$22/month |
| Stopped instances (RDS storage only) | ~$2.30/month |

### 10.3 Cost Optimization Tips

- **Stop EC2 when not in use:** `aws ec2 stop-instances --instance-ids <id>`
- **Stop RDS when not in use:** `aws rds stop-db-instance --db-instance-identifier secureclaims-db`
- **Delete stack when done:** `aws cloudformation delete-stack --stack-name secureclaims-ai`
- **Set billing alerts:** AWS Console → Billing → Budgets → Create budget ($5 threshold)

---

## 🔷 11. Checking Into GitHub

### 11.1 File Structure to Commit

```
infrastructure/
└── cloudformation/
    ├── secureclaims-stack.yaml      ← CloudFormation template
    └── README.md                     ← Quick-start for team members
.kiro/
└── outputs/
    ├── aws-cloudformation-guide.md   ← This detailed guide
    └── aws-deployment-guide.md       ← General deployment guide
```

### 11.2 Commit and Push

```bash
git add infrastructure/ .kiro/outputs/aws-cloudformation-guide.md
git commit -m "feat(infra): add CloudFormation template and deployment guide"
git push origin main
```

### 11.3 What NOT to Commit

| File/Value | Reason |
|------------|--------|
| `.pem` key files | Private SSH keys |
| DB passwords | Secrets |
| JWT secrets | Secrets |
| GitHub PATs | Secrets |
| `.env` files with real values | Contains secrets |

These should be passed as parameters at stack creation time or stored in AWS SSM/Secrets Manager.

---

## 🔶 12. CI/CD Integration

### 12.1 Full Pipeline Flow (After CloudFormation)

```
Developer pushes to main
    │
    ▼
GitHub Actions: Build & Test
    │
    ▼
GitHub Actions: Push Docker images to ghcr.io
    │
    ▼
GitHub Actions: SSH into EC2 → pull latest images → restart containers
    │
    ▼
Application live on AWS with zero downtime
```

### 12.2 Required GitHub Secrets for Deployment

| Secret | Purpose |
|--------|---------|
| `AWS_ACCESS_KEY_ID` | IAM user for CloudFormation/EC2 |
| `AWS_SECRET_ACCESS_KEY` | IAM user secret |
| `EC2_HOST` | EC2 public IP (from stack outputs) |
| `EC2_SSH_KEY` | Contents of .pem file |
| `GH_PAT_PACKAGES` | GitHub PAT with `read:packages` (for EC2 to pull images) |

---

## 🔷 13. Quick Reference Commands

| Action | Command |
|--------|---------|
| Validate template | `aws cloudformation validate-template --template-body file://infrastructure/cloudformation/secureclaims-stack.yaml` |
| Create stack | `aws cloudformation create-stack --stack-name secureclaims-ai --template-body file://... --parameters ... --capabilities CAPABILITY_IAM` |
| Check status | `aws cloudformation describe-stacks --stack-name secureclaims-ai --query 'Stacks[0].StackStatus'` |
| Get outputs | `aws cloudformation describe-stacks --stack-name secureclaims-ai --query 'Stacks[0].Outputs'` |
| Update stack | `aws cloudformation update-stack --stack-name secureclaims-ai --template-body file://... --capabilities CAPABILITY_IAM` |
| Delete stack | `aws cloudformation delete-stack --stack-name secureclaims-ai` |
| SSH into EC2 | `ssh -i secureclaims-keypair.pem ec2-user@<ec2-ip>` |
| View containers | `docker ps` (on EC2) |
| View logs | `docker logs <container-name> --tail 50` (on EC2) |
| Redeploy | `docker-compose pull && docker-compose up -d` (on EC2) |

---

## 🔶 14. Next Steps

After this guide is reviewed:

1. **Generate the CloudFormation template** (`infrastructure/cloudformation/secureclaims-stack.yaml`)
2. **Create EC2 Key Pair** in your AWS account
3. **Generate GitHub PAT** with `read:packages` scope
4. **Execute the stack** using AWS CLI or Console
5. **Verify deployment** with health checks and end-to-end test
6. **Update CI/CD pipeline** to auto-deploy on push to main

---

*Generated: 2026-07-08 | SecureClaims AI | AWS CloudFormation Deployment Guide*
