# Kiro ADLC Step 2 - Architecture Design (Based on Requirements)

## System Name:
SecureClaims AI – Insurance Claims Processing System
---
## Input Dependency:
Use the requirements defined in:
`.kiro/outputs/01-requirements-output.md`
Do NOT redesign requirements — only architect the system based on them and use the technology stack specfied in .kiro/outputs/01-requirements-output.md`
---
## Objective:
Design a scalable microservices architecture that directly maps to the generated requirements.
---
## Required Services:
- Identity Service (Auth, JWT)
- Claims Service (Core business logic)
- Fraud Detection Service (risk scoring)
- Notification Service (alerts)
---
## Key Requirement:
Ensure every functional requirement is mapped to a service responsibility.
---
## Event-Driven Flow:
- Claim submission triggers ClaimCreated event
- Fraud service consumes event and returns FraudScore
- Notification service reacts to status updates
---
## Output Required:
1. Architecture diagram (text-based)
2. Mapping of requirements → services
3. Event flow sequence
4. Service interaction design
5. Justification of design decisions