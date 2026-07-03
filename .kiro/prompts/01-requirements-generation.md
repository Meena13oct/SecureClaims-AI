# Kiro ADLC Step 1 - Requirements Generation

## System Name:
SecureClaims AI – Insurance Claims Processing System

---

## Objective:
Define complete functional and non-functional requirements for a microservices-based insurance claims platform using an AI-driven development lifecycle (ADLC).

The system simulates an event-driven workflow for insurance claim processing, fraud detection, document handling, and notifications.

---

## Business Flow:
- Users register and login
- Users submit insurance claims
- Users upload supporting documents
  - Stored in local file system (uploads/claims/{claimId}/)
  - Supports PDF
  - Metadata stored in database only
- Claims are processed through an event-driven workflow
- Fraud Detection Service evaluates claim risk
- Notification Service updates users on claim status

---

## Architecture Constraints:
- Microservices architecture using Spring Boot
- Identity Service (Authentication & JWT)
- Claims Service (Core claim processing)
- Fraud Detection Service (rule-based scoring engine)
- Notification Service (mock email/SMS delivery)
- Spring Boot Event System (in-memory event-driven communication)
- PostgreSQL database per microservice
- Schema-per-service design for isolation

---

## Backend Technology Stack:

### Core Framework
- Java 17
- Spring Boot 3.x
- Spring Web (REST APIs)
- Spring Data JPA (ORM layer)

---

### Architecture Style
- Microservices Architecture
- Event-Driven Communication using Spring Boot Events (ApplicationEventPublisher)
- Synchronous REST APIs for client interactions

---

### Microservices
- Identity Service (Authentication & Authorization)
- Claims Service (Claim lifecycle management)
- Fraud Detection Service (Rule-based risk scoring)
- Notification Service (User alerts via simulated email/SMS)

---

### Event-Driven Communication
- Spring Boot Events (in-memory event bus)
- Domain Events:
  - ClaimCreatedEvent
  - FraudAnalysisCompletedEvent
  - ClaimStatusUpdatedEvent
- Simple retry mechanism using scheduled jobs (Spring Scheduler)

---

### Database Layer
- PostgreSQL (local or Docker-based)
- Spring Data JPA repositories
- Hibernate ORM
- Schema-per-service database design

---

### Security
- Spring Security
- JWT-based authentication
- Role-based access control (USER, ADMIN)

---

### API Design
- REST APIs (JSON over HTTP)
- OpenAPI / Swagger documentation

---

### File Storage
- Local file system storage for uploaded documents
- Structure: uploads/claims/{claimId}/
- Database stores file metadata and file path only
- Supports PDF and image uploads

---

### Messaging / Eventing
- Spring Boot Events (in-memory communication)
- No external message broker required

---

### Secrets Management
- Spring Boot externalized configuration
- Environment variables for sensitive data
- No hardcoded credentials

---

### Fraud Detection (ML Simulation)
- Spring Boot-based rule engine
- Risk scoring based on:
  - Claim amount
  - Policy age
  - User history
- Output:
  - Low / Medium / High risk classification

---

### Notification Service
- Simulated email and SMS delivery
- Console-based notification output
- Designed for future integration with real providers

---

### Logging & Monitoring
- SLF4J + Logback
- Structured logging for events
- Spring Boot Actuator for health checks and metrics

---

### Testing
- JUnit 5
- Mockito
- Spring Boot Test

---

### Build & Deployment
- Maven build system
- Docker-ready microservices (optional containerization)
- Local deployment focused architecture

---

## Output Required from Kiro:
1. Functional Requirements
2. Non-Functional Requirements
3. System Actors
4. Service Responsibilities
5. Event Flow Description
6. Data Entities Overview

---

## Format:
Structured markdown with clear sections suitable for Git-based ADLC tracking and documentation.