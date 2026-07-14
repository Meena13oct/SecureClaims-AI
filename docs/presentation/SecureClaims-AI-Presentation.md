# SecureClaims AI — Capstone Project Presentation
## Kiro AI IDE Features Used

---

## Slide 1: Title Slide

**SecureClaims AI**
*AI-Powered Insurance Claims Processing System*

- **Built entirely with:** Kiro AI IDE
- **Developer:** Meena
- **Focus:** How Kiro features drove the full development lifecycle

---

## Slide 2: What is Kiro?

- AI-powered IDE built on top of VS Code
- Supports structured development via **ADLC** (AI-Driven Lifecycle)
- Two session types: **Vibe** (conversational) and **Spec** (structured requirements → design → tasks)
- Two autonomy modes: **Autopilot** (end-to-end) and **Supervised** (approve each change)
- Key features: Specs, Steering, Hooks, MCP Powers, Sub-agents

---

## Slide 3: Project Overview — What Was Built

**SecureClaims AI** — A microservices-based insurance claims platform with:
- 4 Spring Boot microservices (Identity, Claims, Fraud Detection, Notification)
- Event-driven fraud scoring engine
- JWT-based security
- CI/CD pipeline with GitHub Actions
- AWS cloud deployment via CloudFormation
- Automated API testing via Postman

**Everything generated and managed through Kiro features.**

---

## Slide 4: Kiro Feature — Spec Sessions (ADLC)

**What I used:** Spec Sessions (Requirements → Design → Tasks)

**What it generated:**

| ADLC Phase | Kiro Generated |
|-----------|----------------|
| Step 1: Requirements | 44 functional + 24 non-functional requirements |
| Step 2: Architecture Design | C4 diagrams, service interactions, sequence diagrams |
| Step 3: User Stories | 20 user stories across 7 epics, 3 sprints |
| Step 4: API Design | Endpoint specifications for all services |
| Step 5: Implementation Tasks | Task breakdown for each user story |

**How:** I provided prompts in `.kiro/prompts/` and Kiro produced the full output documents that guided all implementation.

---

## Slide 5: Kiro Feature — Autopilot Mode (Code Generation)

**What I used:** Autopilot Mode for implementation

**What it generated:**
- Complete Maven multi-module project structure
- All 4 microservices with controller → service → repository layers
- JPA entities, DTOs, event classes, exception handlers
- Spring Security configuration with JWT filter chain
- Fraud rule engine with configurable scoring
- Event-driven notification system
- Database schema scripts (CREATE TABLE) + Flyway migrations
- Automatic table creation in PostgreSQL via Hibernate DDL-auto
- Dockerfiles for each service
- docker-compose.yaml for local development

**How:** Kiro read the spec outputs (requirements, design, tasks) and implemented each user story autonomously, service by service, following the task order.

---

## Slide 6: Kiro Feature — Steering Rules

**What I used:** Steering files (`.kiro/steering/`)

**What they do — guide Kiro's behavior across ALL interactions:**

| Steering File | What It Controls |
|--------------|-----------------|
| `coding-standards.md` | Java naming conventions, architecture rules (no logic in controllers, constructor injection only), class size limits, Javadoc requirements |
| `api-standards.md` | REST API design: versioning format, standard error response shape, pagination pattern, validation annotations |
| `service-operations.md` | Docker Compose commands for start/stop/rebuild — Kiro follows this when managing containers |
| `auto-push.md` | After any file save, Kiro auto-stages, commits, and pushes to main branch |

**Impact:** Kiro consistently followed these rules across all 4 services without me repeating them. One-time setup, permanent enforcement.

---

## Slide 7: Kiro Feature — Hooks (Automated API Testing)

**What I used:** Agent Hook — `postman-api-testing.kiro.hook`

**Trigger:** Any `.java`, `.yml`, or `pom.xml` file is edited

**What it does:**
1. Detects that API source code changed
2. Retrieves the Postman collection configuration
3. Runs the Postman collection tests automatically
4. Shows results and proposes fixes for any failures

**Result:** Every time I modify a controller or config, API tests run automatically without me triggering anything.

---

## Slide 8: Kiro Feature — Hooks (Self-Healing CI/CD)

**What I used:** Agent Hook — `verify-build-post-push.kiro.hook`

**Trigger:** After any shell command (detects `git push`)

**What it does:**
1. Monitors GitHub Actions build status (polls until complete)
2. If build fails → pulls error logs → analyzes root cause → fixes code → pushes again
3. This creates a self-healing loop (push → fail → fix → push → pass)
4. Once build succeeds → deletes all old workflow runs to keep history clean

**Result:** I never manually debug CI failures. Kiro detects, fixes, and re-pushes until green.

---

## Slide 9: Kiro Feature — Hooks (Auto-Push on Save)

**What I used:** Steering rule `auto-push.md` + Kiro's autopilot

**Trigger:** Any core file saved (*.java, *.yml, pom.xml, Dockerfile)

**What it does:**
1. Verifies local code health
2. Stages all changes
3. Commits with a meaningful message
4. Pushes directly to remote main branch

**Result:** Zero manual git workflow. I focus on telling Kiro what to build — it handles version control automatically.

---

## Slide 10: Kiro Feature — MCP Power (Postman)

**What I used:** Postman MCP Power

**What it did:**
- Created a Postman workspace (`secureclaims`)
- Created 4 collections (one per microservice)
- Generated 16+ test requests for Identity Service mapped to user stories
- Created test scripts with assertions (status codes, response structure)
- Set up environments (Local + AWS Production)
- Ran collections and reported results

**How:** I activated the Postman power and Kiro used it to programmatically create and execute API tests without me opening the Postman app.

---

## Slide 11: Kiro Feature — Sub-Agents

**What I used:** Context-gatherer and general-task-execution sub-agents

**What they did:**
- **Context-gatherer:** When starting work on a new service or investigating a bug, Kiro spawned a sub-agent to explore the codebase and identify relevant files before making changes
- **General-task-execution:** For parallel implementation — multiple user stories executed simultaneously by delegating to sub-agents

**Result:** Kiro worked like a team — one agent orchestrating, others implementing in parallel.

---

## Slide 12: Kiro Feature — Prompt-Driven Task Execution

**What I used:** Implementation prompt (`.kiro/prompts/07-user-stories-implementation.md`)

**How it worked:**
1. Kiro reads user stories from `03-user-stories-output.md` in sprint/epic order
2. For each user story, Kiro follows a strict sequence:
   - Analyze → API Design → DB Schema (CREATE TABLE scripts) → Backend Code → Tests → Seed Data
   - Kiro generated Flyway migration scripts per service; Flyway creates the tables in PostgreSQL at startup
3. After implementing each story, Kiro **generates an execution report** (`docs/reports/epics/{service}/US-XXX-report.md`) documenting what was built, test results, and Postman testing guide
4. After the report, Kiro **auto-creates Postman test cases** in the matching collection based on the report's testing guide
5. Only moves to the next story after the current one is fully complete

**Result:** 20 user stories implemented in order — each with code, tests, execution report, and Postman tests generated automatically.

---

## Slide 13: Kiro Feature — CI/CD Pipeline Generation

**What I used:** Kiro Autopilot with prompt

**What it generated:**
- Complete GitHub Actions workflow (`ci-cd.yml`)
- Multi-stage pipeline: Build & Test → Docker Build → Push to GHCR → Deploy to EC2
- PostgreSQL service container for integration tests
- Maven caching for faster builds
- SSH-based auto-deploy to AWS EC2

**How:** I provided a prompt (`.kiro/prompts/05-generate-pipeline.md`) and Kiro produced the full working pipeline file.

---

## Slide 14: Kiro Feature — Infrastructure as Code & Deployment Guide

**What I used:** Kiro Autopilot for AWS CloudFormation

**What it generated:**
- Full CloudFormation stack (`infrastructure/cloudformation/secureclaims-stack.yaml`) — VPC, EC2, RDS, Security Groups, IAM, Elastic IP
- AWS Deployment Guide (`aws-deployment-guide.md`) — step-by-step instructions to deploy the full stack
- Cloud Demo Guide (`cloud-demo-guide.md`) — how to demo the live system on AWS

**How:** I asked Kiro to generate cloud infrastructure and deployment documentation. It produced production-ready IaC optimized for AWS Free Tier, plus guides to deploy and demo it — all from a single conversation.

---

## Slide 15: Kiro Features Summary — What Generated What

| Kiro Feature | What It Generated / Did |
|-------------|------------------------|
| **Spec Sessions** | Requirements, architecture, user stories, API design |
| **Autopilot Mode** | Full microservices codebase (4 services) |
| **Steering Rules** | Consistent code quality, auto-push, API standards |
| **Hook: API Testing** | Automatic Postman test execution on code changes |
| **Hook: Self-Healing CI** | Auto-fix build failures and cleanup old runs |
| **Hook: Auto-Push** | Zero-touch git workflow (stage → commit → push) |
| **MCP Power: Postman** | Postman collections, test scripts, environments |
| **Sub-Agents** | Parallel task execution, codebase exploration |
| **Prompts** | DB migration scripts, CI/CD pipeline, Dockerfiles, CloudFormation stack |

---

## Slide 16: The Development Flow with Kiro

```
1. Write prompts (.kiro/prompts/)
         │
         ▼
2. Kiro generates requirements + design + user stories (Spec Session)
         │
         ▼
3. Set up steering rules (coding standards, API standards, auto-push)
         │
         ▼
4. Kiro implements user stories in order (Autopilot + Prompt-Driven Tasks)
         │
         ▼
5. Kiro generates DB migration scripts (Flyway creates tables at service startup)
         │
         ▼
6. After each story → generates execution report + Postman test cases
         │
         ▼
7. On every file save → auto-push to GitHub (Steering: auto-push)
         │
         ▼
8. On every push → monitor build + auto-fix failures (Hook: self-healing CI)
         │
         ▼
9. On every code change → run API tests automatically (Hook: Postman testing)
         │
         ▼
10. Generate infrastructure + deploy to AWS (Autopilot + CloudFormation)
```

**My role:** Provide the initial prompts and review. Kiro did the rest.

---

## Slide 17: Thank You

**SecureClaims AI — Built End-to-End with Kiro**

**Key Takeaway:** Kiro isn't just a code assistant — it's a full development lifecycle tool that handles requirements, design, implementation, testing, CI/CD, and deployment through its features (Specs, Steering, Hooks, Powers, Sub-agents).

---
