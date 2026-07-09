# SecureClaims AI — Cloud Demo Guide

## Purpose

This guide helps you demo the SecureClaims AI project from your **org laptop** (where you cannot install anything) by running everything in the cloud. You only need a **web browser** on your org laptop.

---

## Demo Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        YOUR ORG LAPTOP                               │
│                     (Browser Only - No Installs)                      │
│                                                                     │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────────────────────┐  │
│  │  Postman    │  │  GitHub      │  │  AWS Console              │  │
│  │  (Web App)  │  │  (Web)       │  │  (Web)                    │  │
│  └──────┬──────┘  └──────┬───────┘  └────────────┬──────────────┘  │
└─────────│────────────────│────────────────────────│──────────────────┘
          │                │                        │
          ▼                ▼                        ▼
┌──────────────────────────────────────────────────────────────────────┐
│                           AWS CLOUD                                    │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  EC2 t2.micro (Docker Compose)                                 │  │
│  │                                                                │  │
│  │  identity-service:8081    claims-service:8082                   │  │
│  │  fraud-detection-service:8083    notification-service:8084      │  │
│  └─────────────────────────────┬──────────────────────────────────┘  │
│                                │                                     │
│  ┌─────────────────────────────▼──────────────────────────────────┐  │
│  │  RDS PostgreSQL db.t3.micro                                    │  │
│  │  secureclaims DB (schemas: identity, claims, fraud, notif)     │  │
│  └────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
```

---

## What You Need on Your Org Laptop

| Tool | How to Access | Purpose |
|------|---------------|---------|
| Web Browser | Already installed | Access all tools below |
| Postman Web | https://web.postman.co | Run API tests, view collections |
| GitHub Web | https://github.com | View code, trigger CI/CD, view pipeline |
| AWS Console | https://console.aws.amazon.com | Monitor infrastructure, view EC2 |

**Zero installations required.**

---

## Pre-Demo Setup (Do This from Your Personal Laptop)

### Step 1: Deploy to AWS

Follow the [AWS CloudFormation Guide](./aws-cloudformation-guide.md) to create the stack. After completion, note:

- **EC2 Public IP:** `<your-ec2-ip>` (from stack outputs)
- **RDS Endpoint:** `<your-rds-endpoint>` (from stack outputs)

### Step 2: Verify Services Are Running

From your personal laptop (or AWS CloudShell):

```bash
curl http://<ec2-ip>:8081/actuator/health
curl http://<ec2-ip>:8082/actuator/health
curl http://<ec2-ip>:8083/actuator/health
curl http://<ec2-ip>:8084/actuator/health
```

All should return `{"status":"UP"}`.

### Step 3: Update Postman Collection URLs

Update the Postman requests to point to your EC2 public IP instead of `localhost`. You can do this via:

**Option A:** Create a Postman environment called "AWS Cloud" with variable:
- `BASE_URL` = `http://<ec2-ip>`

**Option B:** Update requests directly in the Postman Web UI at https://web.postman.co

### Step 4: Push All Code to GitHub

```bash
git add .
git commit -m "feat: complete project ready for cloud demo"
git push origin main
```

---

## Demo Flow (From Org Laptop — Browser Only)

### Demo Section 1: Show the Architecture & Code (GitHub Web)

1. Open https://github.com/meena13oct/SecureClaims-AI
2. Walk through the project structure:
   - `identity-service/` — User registration, login, JWT
   - `claims-service/` — Claims CRUD, document upload
   - `fraud-detection-service/` — Fraud scoring engine
   - `notification-service/` — Event-driven notifications
3. Show key files:
   - `docker-compose.yaml` — Local development setup
   - `.github/workflows/ci-cd.yml` — CI/CD pipeline
   - `infrastructure/cloudformation/secureclaims-stack.yaml` — IaC
4. Show the execution reports under `docs/reports/epics/`

---

### Demo Section 2: Show CI/CD Pipeline (GitHub Actions)

1. Navigate to **Actions** tab in GitHub
2. Show the latest successful workflow run
3. Walk through the pipeline stages:
   - Build & Test (with PostgreSQL service container)
   - Docker image build and push to ghcr.io
4. Show the Docker images under **Packages** tab

---

### Demo Section 3: Show Cloud Infrastructure (AWS Console)

1. Open https://console.aws.amazon.com
2. Navigate to **CloudFormation** → Show the `secureclaims-ai` stack
3. Show **Resources** tab — all provisioned infrastructure
4. Navigate to **EC2** → Show the running instance
5. Navigate to **RDS** → Show the managed PostgreSQL instance
6. Show **Security Groups** — network isolation between EC2 and RDS

---

### Demo Section 4: Live API Testing (Postman Web)

1. Open https://web.postman.co
2. Navigate to the **secureclaims** workspace
3. Open the **identityservice** collection

#### Test 1: Register a New User (201 Created)

- Open `US-003: Register New User (Success - 201)`
- Click **Send**
- Show the response: 201 with user details and USER role
- Show the test results passing (green checkmarks)

#### Test 2: Validation Error (400 Bad Request)

- Open `US-003: Register with Missing Fields (Validation - 400)`
- Click **Send**
- Show the response: 400 with field-level errors
- Show test assertions passing

#### Test 3: Duplicate Email (409 Conflict)

- Open `US-003: Register with Duplicate Email (Conflict - 409)`
- Click **Send**
- Show the response: 409 with conflict message
- Show test assertions passing

#### Test 4: User Login (200 OK)

- Open the login request
- Click **Send**
- Show the JWT token in the response
- Copy the token for use in subsequent requests

#### Test 5: Authenticated Requests (Claims Service)

- Open the **claimsservice** collection
- Set the Authorization header with the JWT from login
- Show authenticated requests working

---

### Demo Section 5: Run Full Collection (Postman Collection Runner)

1. In Postman Web, click **Run** on the identityservice collection
2. Show the Collection Runner executing all requests in sequence
3. Show the test results summary:
   - Total tests, passed, failed
   - Response times
   - Status codes

---

### Demo Section 6: Show Kiro AI Development Workflow (Optional)

If you have access to Kiro on your org laptop via browser (cloud IDE):

1. Show the `.kiro/prompts/07-user-stories-implementation.md` — the automated workflow
2. Explain how implementing a user story automatically:
   - Generates the code (controller, service, repository, DTOs)
   - Runs tests
   - Generates an execution report
   - Creates Postman test cases via the Postman MCP power
3. Show the `.kiro/hooks/postman-api-testing.kiro.hook` — auto-testing on file save

---

## Demo Script (5-Minute Version)

| Time | What to Show | Where |
|------|-------------|-------|
| 0:00 | Project overview & microservices architecture | GitHub Web |
| 1:00 | CI/CD pipeline (GitHub Actions) | GitHub Actions tab |
| 2:00 | Cloud infrastructure (CloudFormation stack, EC2, RDS) | AWS Console |
| 3:00 | Live API testing — Register, Login, Get JWT | Postman Web |
| 4:00 | Run full collection — automated test results | Postman Collection Runner |
| 5:00 | Wrap up — show test coverage, reports | GitHub Web |

---

## Demo Script (15-Minute Version)

| Time | What to Show | Where |
|------|-------------|-------|
| 0:00 | Introduction — problem statement & solution | Slide/verbal |
| 1:00 | Architecture diagram — microservices, DB, cloud | GitHub README |
| 3:00 | Code walkthrough — clean architecture, layers | GitHub Web |
| 5:00 | CI/CD pipeline — auto build, test, Docker push | GitHub Actions |
| 7:00 | Infrastructure as Code — CloudFormation template | GitHub Web |
| 8:00 | Cloud deployment — running services on AWS | AWS Console |
| 10:00 | Live API demo — Register → Login → Get JWT → Claims | Postman Web |
| 12:00 | Automated testing — Collection Runner results | Postman Web |
| 13:00 | AI-assisted development — Kiro workflow & automation | GitHub (show .kiro/) |
| 15:00 | Q&A | — |

---

## Troubleshooting During Demo

| Problem | Quick Fix |
|---------|-----------|
| API returns connection refused | EC2 may be stopped → Start it from AWS Console |
| Postman shows timeout | Check EC2 security group allows ports 8081-8084 |
| 409 Conflict on register | User already exists — use a different email or clear DB |
| JWT expired | Re-login to get a fresh token |
| Services unhealthy | SSH via AWS CloudShell: `docker restart <container>` |
| RDS connection error | Check RDS is not stopped (AWS auto-stops after 7 days) |

---

## AWS CloudShell (Emergency Access — No SSH Key Needed)

If you need to run commands on EC2 during the demo without SSH keys:

1. Open AWS Console → search **CloudShell** (top bar)
2. Use SSM Session Manager (if configured):
   ```bash
   aws ssm start-session --target <ec2-instance-id> --region ap-south-1
   ```
3. Or use CloudShell to run diagnostic commands:
   ```bash
   # Check EC2 status
   aws ec2 describe-instance-status --instance-ids <id> --region ap-south-1
   
   # Test connectivity
   curl http://<ec2-ip>:8081/actuator/health
   ```

---

## Pre-Demo Checklist

Run through this the night before your demo:

- [ ] AWS stack is in `CREATE_COMPLETE` state
- [ ] EC2 instance is running (not stopped)
- [ ] RDS instance is available (not stopped)
- [ ] All 4 health endpoints return `{"status":"UP"}`
- [ ] Postman collections point to EC2 IP (not localhost)
- [ ] GitHub repo is up to date with latest code
- [ ] Tested a full Register → Login → JWT → Claims flow
- [ ] Postman Collection Runner passes all tests
- [ ] Bookmarked all URLs on org laptop browser:
  - GitHub repo
  - GitHub Actions
  - AWS Console (CloudFormation)
  - Postman Web (secureclaims workspace)

---

## Key URLs to Bookmark on Org Laptop

| Purpose | URL |
|---------|-----|
| GitHub Repo | `https://github.com/meena13oct/SecureClaims-AI` |
| GitHub Actions | `https://github.com/meena13oct/SecureClaims-AI/actions` |
| AWS Console | `https://ap-south-1.console.aws.amazon.com/cloudformation` |
| Postman Web | `https://web.postman.co` → secureclaims workspace |
| Identity Service | `http://<ec2-ip>:8081/actuator/health` |
| Claims Service | `http://<ec2-ip>:8082/actuator/health` |
| Fraud Service | `http://<ec2-ip>:8083/actuator/health` |
| Notification Service | `http://<ec2-ip>:8084/actuator/health` |

---

## Cost Reminder

- All services run on AWS Free Tier ($0/month for 12 months)
- **Stop EC2 and RDS after demo** to preserve free tier hours:
  ```bash
  aws ec2 stop-instances --instance-ids <id> --region ap-south-1
  aws rds stop-db-instance --db-instance-identifier secureclaims-db --region ap-south-1
  ```
- Or delete the entire stack when no longer needed:
  ```bash
  aws cloudformation delete-stack --stack-name secureclaims-ai --region ap-south-1
  ```

---

*Generated: 2026-07-08 | SecureClaims AI | Cloud Demo Guide — Browser Only*
