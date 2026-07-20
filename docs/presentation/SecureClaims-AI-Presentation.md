# SecureClaims AI — Capstone Project Presentation
## Kiro AI IDE Features & ADLC Workflow

---

## Slide 1: Title Slide

**SecureClaims AI**
*AI-Powered Insurance Claims Processing System*

- **Built entirely with:** Kiro AI IDE
- **Developer:** Meena
- **Methodology:** ADLC (AI-Driven Development Lifecycle)
- **Focus:** How Kiro features drove the full development lifecycle from idea to production

---

## Slide 2: What is Kiro?

- AI-powered IDE built on top of VS Code
- Supports structured development via **ADLC** (AI-Driven Development Lifecycle)
- Two session types: **Vibe** (conversational) and **Spec** (structured requirements → design → tasks)
- Two autonomy modes: **Autopilot** (end-to-end) and **Supervised** (approve each change)
- Key features: Specs, Steering, Hooks, MCP Powers, Agents

---

## Slide 3: Project Overview — What Was Built

**SecureClaims AI** — A microservices-based insurance claims platform with:
- 4 Spring Boot microservices (Identity, Claims, Fraud Detection, Notification)
- Event-driven fraud scoring engine
- JWT-based security
- CI/CD pipeline with GitHub Actions
- AWS cloud deployment via CloudFormation
- Automated API testing via Postman

**Everything generated and managed through Kiro's ADLC workflow.**

---

## Slide 4: The ADLC (AI-Driven Development Lifecycle)

```
┌──────────────────────────────────────────────────────────────────┐
│                     ADLC WORKFLOW                                  │
│                                                                    │
│   PROMPTS          GENERATE              DECOMPOSE                │
│  (.kiro/prompts/)  (.kiro/outputs/ +     (.kiro/specs/)           │
│   Input prompts     .kiro/architecture/)  Per-service breakdown   │
│   telling Kiro      Full system-level     requirements.md         │
│   what to produce   documents generated   design.md               │
│                                           tasks.md                │
│        │                   │                    │                  │
│        ▼                   ▼                    ▼                  │
│   IMPLEMENT          VERIFY               DEPLOY                  │
│  (src/**)            (hooks/ +            (.github/workflows/ +   │
│   Code generated     steering/)            Dockerfile +           │
│   guided by specs    Quality enforced      CloudFormation)        │
│   and steering       at every step         Auto CI/CD             │
└──────────────────────────────────────────────────────────────────┘
```

**My role:** Provide the initial prompts. Kiro did the rest.

---

## Slide 5: ADLC Step 1 — Prompts (Input)

**Location:** `.kiro/prompts/`

**What I wrote — the instructions for each generation step:**

| Prompt File | Purpose |
|-------------|---------|
| `01-requirements-generation.md` | Generate functional & non-functional requirements |
| `02-architecture-design.md` | Generate architecture diagrams and service interactions |
| `03-user-stories.md` | Generate user stories with acceptance criteria |
| `04-docker-compose-postgres.md` | Generate Docker Compose with PostgreSQL setup |
| `05-generate-pipeline.md` | Generate CI/CD GitHub Actions pipeline |
| `06-generate-docker-files.md` | Generate Dockerfiles for each microservice |
| `07-user-stories-implementation.md` | Implementation execution plan |
| `08-execution-report-format.md` | Report format after each story |
| `start-services.md` / `stop-services.md` | Service management prompts |
| `health-check.md` | System health verification |

---

## Slide 6: ADLC Step 2 — Generate (System-Level Outputs)

**Location:** `.kiro/outputs/` + `.kiro/architecture/`

**What Kiro produced — the full consolidated system documents:**

| Generated Document | Content |
|-------------------|---------|
| `01-requirements-output.md` | 44 functional + 24 non-functional requirements for the entire system |
| `02-architecture-design-output.md` | C4 diagrams (Context, Container, Component, Deployment), service interaction design, security architecture |
| `03-user-stories-output.md` | 20 user stories across 7 epics, 3 sprints, with acceptance criteria |
| `04-api-design-output.md` | 14 REST endpoints with request/response schemas, status codes, validation rules |
| `aws-deployment-guide.md` | Step-by-step AWS deployment instructions |
| `aws-cloudformation-guide.md` | IaC stack documentation |
| `cloud-demo-guide.md` | Live demo script for AWS-deployed system |

**Key point:** These are the "monolith" system-wide documents — one place to see everything.

---

## Slide 7: ADLC Step 3 — Decompose (Per-Service Specs)

**Location:** `.kiro/specs/` — **Visible in Kiro's Specs Panel**

The system-level documents were decomposed into per-service specs for actionable implementation:

```
.kiro/specs/
├── identity-management/
│   ├── requirements.md     ← FR-001 to FR-013 (identity only)
│   ├── design.md           ← Architecture, API endpoints, security design
│   └── tasks.md            ← US-003, 004, 005, 006, 016, 017, 018
├── claims-processing/
│   ├── requirements.md     ← FR-014 to FR-028 (claims only)
│   ├── design.md           ← Architecture, event flow, file storage
│   └── tasks.md            ← US-007, 008, 009, 010, 012, 017, 018
├── fraud-detection/
│   ├── requirements.md     ← FR-029 to FR-037 (fraud only)
│   ├── design.md           ← Scoring rules, event-driven flow
│   └── tasks.md            ← US-011, 015, 017, 018
└── notifications/
    ├── requirements.md     ← FR-038 to FR-044 (notifications only)
    ├── design.md           ← Event listeners, sender abstraction
    └── tasks.md            ← US-013, 014, 015, 017, 018
```

**Each spec has:** requirements (what) → design (how) → tasks (checklist with [x] completion status)

**Why decompose?** Developers work on one service at a time. Each service gets only its own scope — clear, focused, actionable.

---

## Slide 8: ADLC Step 4 — Implement (Steering-Guided Code Generation)

**What I used:** Autopilot Mode + Steering Rules

**Steering files (`.kiro/steering/`) — permanent behavior rules for Kiro:**

| Steering File | What It Enforces |
|--------------|-----------------|
| `coding-standards.md` | Java naming, architecture rules (no logic in controllers, constructor injection only), class size limits, Javadoc, exception handling |
| `api-standards.md` | REST design: versioning `/api/{service}/v1/`, standard error envelope, pagination, Bean Validation |
| `service-operations.md` | Docker Compose commands for start/stop/rebuild — how Kiro manages containers |
| `auto-push.md` | On file save → auto-stage, commit, push to main branch |
| `log-observer.md` | Log analysis patterns, severity classification, fix suggestions |

**What Kiro generated during implementation:**
- Complete Maven multi-module project structure
- All 4 microservices with controller → service → repository layers
- JPA entities, DTOs, event classes, exception handlers
- Spring Security configuration with JWT filter chain
- Fraud rule engine with configurable scoring
- Event-driven notification system
- Database scripts + Hibernate DDL-auto
- Dockerfiles + docker-compose.yaml

---

## Slide 9: ADLC Step 5 — Verify (Hooks = Automated Quality Gates)

**Location:** `.kiro/hooks/`

Hooks are Kiro's automation triggers — they fire automatically at specific points in the workflow:

| Hook | Trigger | What It Does |
|------|---------|--------------|
| **Code Standards Reviewer** | `preToolUse` (before any file write) | Reviews code against coding-standards.md before writing — architecture rules, naming, Javadoc, size limits, security |
| **API Postman Testing** | `fileEdited` (*.java, *.yml, pom.xml) | Runs Postman collection tests automatically when API code changes |
| **Verify & Fix Build After Push** | `postToolUse` (after shell command = git push) | Monitors GitHub Actions, auto-fixes failures, re-pushes until green, cleans old runs |
| **Security Diff-Scan Suggester** | `agentStop` (after agent completes work) | Evaluates if changes are security-sensitive → triggers AWS Security Agent diff scan |

---

## Slide 10: Hook Deep-Dive — Code Standards Reviewer

**File:** `code-reviewer.kiro.hook`
**Trigger:** `preToolUse` — fires BEFORE any write operation

**What it checks (from coding-standards.md):**
1. Architecture Rules — No business logic in controllers, no entities in API responses
2. Dependency Injection — Constructor injection only, no @Autowired
3. Naming Conventions — PascalCase, camelCase, correct suffixes
4. Javadoc — @author + @since on every class
5. Method/Class Size — max 20 lines per method, class limits
6. Exception Handling — Custom exceptions, no inline catches
7. Security — No hardcoded secrets, @PreAuthorize on admin endpoints

**Result:** Every line of code is validated before it touches the filesystem — zero violations get through.

---

## Slide 11: Hook Deep-Dive — Self-Healing CI/CD

**File:** `verify-build-post-push.kiro.hook`
**Trigger:** `postToolUse` — fires after every shell command (detects `git push`)

**Flow:**
```
git push detected
    │
    ▼
Poll GitHub Actions (gh run list) until complete
    │
    ├── Build PASSED → Delete old workflow runs → Done
    │
    └── Build FAILED → Pull error logs (gh run view --log-failed)
                          │
                          ▼
                     Analyze root cause
                          │
                          ├── JaCoCo coverage failure → Generate tests → Re-push
                          └── Code failure → Fix code → Re-push (loops back)
```

**Result:** Self-healing loop until build is green. I never manually debug CI failures.

---

## Slide 12: Hook Deep-Dive — Security Diff-Scan

**File:** `security-diff-scan-suggester.kiro.hook`
**Trigger:** `agentStop` — fires when the agent completes its work

**What it evaluates:**
- Does untrusted input flow into a sensitive sink? (SQL, shell, filesystem, auth)
- Were authentication, authorization, or cryptography modified?
- Were security configurations changed? (CORS, headers, session)

**If security-sensitive + complete:** Automatically triggers AWS Security Agent diff scan (no permission needed).

**What it ignores:** Simple reads with parameterized queries, test/doc changes, refactoring.

---

## Slide 13: MCP Powers — External Tool Integration

**Location:** `.kiro/settings/mcp.json`

**Configured MCP Server:**

```json
{
  "mcpServers": {
    "security-agent": {
      "command": "uvx",
      "args": ["awslabs.security-agent-mcp-server@latest"],
      "env": {
        "AWS_REGION": "ap-south-1",
        "WORKSPACE_ROOT": "."
      }
    }
  }
}
```

**AWS Security Agent MCP — What it provides:**
- Code security scanning (full repo scan)
- Diff-based security scanning (only changed code)
- Threat model review against design specs
- Pentest operations
- Scan status monitoring and findings retrieval

**Postman MCP Power — What it did:**
- Created Postman workspace and collections
- Generated 16+ test requests mapped to user stories
- Created test scripts with assertions
- Set up environments (Local + AWS Production)
- Ran collections and reported results

---

## Slide 14: Agents — Specialized AI Workers

**Location:** `.kiro/agents/`

| Agent | Purpose | Configuration |
|-------|---------|---------------|
| **default** | Main development agent — auto-pushes changes after every response | On agent stop: `git add -A → commit → push` |
| **service-ops** | Service operations — start/stop/verify Docker containers | On spawn: runs `docker compose ps` to check current state |
| **log-observer** | Analyzes Docker Compose logs for errors, security issues, performance | Uses patterns from `steering/log-observer.md` |

**Default Agent auto-push behavior:**
```
Agent finishes responding
    → git status --porcelain
    → git add -A
    → git commit -m "chore: auto-push changes after agent response"
    → git push
```

**Service-Ops Agent — tools allowed:** `shell`, `read`, `web_fetch` only (scoped for safety)

---

## Slide 15: Complete .kiro/ Folder Structure

```
.kiro/
├── specs/                    ← ADLC: Per-service requirements → design → tasks (Specs Panel)
│   ├── identity-management/
│   ├── claims-processing/
│   ├── fraud-detection/
│   └── notifications/
├── prompts/                  ← ADLC: Input prompts (what I asked Kiro to generate)
├── outputs/                  ← ADLC: System-wide generated documents
├── architecture/             ← ADLC: Full architecture + user stories + API design
├── steering/                 ← Behavior Rules: coding standards, API standards, ops, auto-push
├── hooks/                    ← Automation: code review, CI/CD, security scan, API testing
├── agents/                   ← Specialized Workers: default, service-ops, log-observer
└── settings/                 ← MCP Powers: AWS Security Agent
```

---

## Slide 16: ADLC Flow — End to End

```
1. Write prompts (.kiro/prompts/)
         │
         ▼
2. Kiro generates system-wide docs (outputs/ + architecture/)
         │
         ▼
3. Decompose into per-service specs (.kiro/specs/) — shows in Kiro Specs Panel
         │
         ▼
4. Set up steering rules (coding standards, API standards, auto-push)
         │
         ▼
5. Kiro implements user stories in sprint order (Autopilot + specs/*/tasks.md)
         │
         ▼
6. Before every file write → code reviewed against standards (Hook: code-reviewer)
         │
         ▼
7. On every file save → auto-push to GitHub (Agent: default + Steering: auto-push)
         │
         ▼
8. On every push → monitor build + auto-fix failures (Hook: verify-build-post-push)
         │
         ▼
9. On code change → run API tests automatically (Hook: postman-api-testing)
         │
         ▼
10. After agent work → security scan if needed (Hook: security-diff-scan-suggester)
         │
         ▼
11. Generate infrastructure + deploy to AWS (CloudFormation + CI/CD pipeline)
```

---

## Slide 17: Kiro Features Summary — Complete Map

| Kiro Feature | What It Generated / Did | Location |
|-------------|------------------------|----------|
| **Spec Sessions (ADLC)** | Requirements, architecture, user stories, API design | `specs/`, `outputs/`, `architecture/` |
| **Autopilot Mode** | Full microservices codebase (4 services) | `src/**` |
| **Steering: coding-standards** | Consistent code quality enforcement | `.kiro/steering/coding-standards.md` |
| **Steering: api-standards** | REST API design consistency | `.kiro/steering/api-standards.md` |
| **Steering: service-operations** | Docker Compose management guide | `.kiro/steering/service-operations.md` |
| **Steering: auto-push** | Zero-touch git workflow | `.kiro/steering/auto-push.md` |
| **Steering: log-observer** | Log analysis patterns and fix templates | `.kiro/steering/log-observer.md` |
| **Hook: Code Reviewer** | Pre-write code quality gate | `.kiro/hooks/code-reviewer.kiro.hook` |
| **Hook: API Testing** | Auto Postman tests on code changes | `.kiro/hooks/postman-api-testing.kiro.hook` |
| **Hook: Self-Healing CI** | Auto-fix build failures + cleanup | `.kiro/hooks/verify-build-post-push.kiro.hook` |
| **Hook: Security Scan** | Auto security analysis on sensitive changes | `.kiro/hooks/security-diff-scan-suggester.kiro.hook` |
| **MCP: AWS Security Agent** | Code scanning, threat modeling, pentesting | `.kiro/settings/mcp.json` |
| **MCP: Postman** | Collections, test scripts, environments | Postman workspace |
| **Agent: default** | Auto-push after every response | `.kiro/agents/default.json` |
| **Agent: service-ops** | Docker container management | `.kiro/agents/service-ops.json` |
| **Agent: log-observer** | Docker log analysis and issue detection | `.kiro/agents/log-observer.md` |
| **Prompts** | DB scripts, CI/CD pipeline, Dockerfiles, CloudFormation | `.kiro/prompts/` |

---

## Slide 18: Thank You

**SecureClaims AI — Built End-to-End with Kiro's ADLC**

**Key Takeaway:** Kiro isn't just a code assistant — it's a full AI-Driven Development Lifecycle tool that handles:

- **Requirements** → Spec Sessions
- **Design** → Architecture generation
- **Implementation** → Autopilot with Steering rules
- **Quality** → Hooks (code review, security scan)
- **Testing** → Hooks (Postman API testing)
- **CI/CD** → Hooks (self-healing pipeline)
- **Deployment** → MCP Powers + CloudFormation
- **Operations** → Agents (service-ops, log-observer)

**From idea to production — one IDE, one methodology: ADLC.**

---
