# Identity Management Service – Design

## Architecture

- **Service:** Identity Service
- **Port:** 8081
- **Base URL:** `http://localhost:8081/api/identity/v1`
- **Schema:** `identity`
- **Technology:** Java 17, Spring Boot 3.x, Spring Security, JWT (jjwt), Spring Data JPA, PostgreSQL

## Component Architecture

```
┌─────────────────────────────────────────────┐
│           Identity Service (:8081)           │
│                                             │
│  Controller Layer                           │
│  ├── AuthController (register, login, me)   │
│  └── AdminUserController (list, status)     │
│                                             │
│  Service Layer                              │
│  ├── AuthService (register, login)          │
│  └── UserService (findAll, toggleStatus)    │
│                                             │
│  Security Layer                             │
│  ├── JwtTokenProvider (generate, validate)  │
│  ├── JwtAuthenticationFilter (filter chain) │
│  └── SecurityConfig (HttpSecurity)          │
│                                             │
│  Repository Layer                           │
│  ├── UserRepository                         │
│  └── RoleRepository                         │
│                                             │
│  Database: PostgreSQL (schema: identity)    │
│  ├── users                                  │
│  ├── roles                                  │
│  └── user_roles                             │
└─────────────────────────────────────────────┘
```

## API Endpoints

### `POST /api/identity/v1/auth/register` (Public)
Register a new user account.

**Request:**
```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane.doe@example.com",
  "username": "janedoe",
  "password": "SecureP@ss1"
}
```

**Response (201):**
```json
{
  "timestamp": "2026-07-04T10:00:00Z",
  "status": 201,
  "message": "User registered successfully",
  "data": {
    "userId": "uuid",
    "username": "janedoe",
    "email": "jane.doe@example.com",
    "roles": ["USER"]
  }
}
```

### `POST /api/identity/v1/auth/login` (Public)
Authenticate and receive JWT token.

**Request:**
```json
{
  "email": "jane.doe@example.com",
  "password": "SecureP@ss1"
}
```

**Response (200):**
```json
{
  "timestamp": "2026-07-04T10:05:00Z",
  "status": 200,
  "message": "Login successful",
  "data": { "token": "eyJ..." }
}
```

### `GET /api/identity/v1/auth/me` (USER)
Return current user profile from JWT.

### `GET /api/identity/v1/admin/users` (ADMIN)
List all registered users with pagination.

### `PUT /api/identity/v1/admin/users/{id}/status` (ADMIN)
Activate or deactivate a user account.

## Security Design

- JWT signed with HMAC-SHA256, 24-hour expiry
- `JwtAuthenticationFilter` extends `OncePerRequestFilter`
- Filter reads `Authorization: Bearer <token>` header
- Valid token → populates `SecurityContextHolder`
- Invalid/missing token → 401 Unauthorized
- `SessionCreationPolicy.STATELESS` — no server sessions
- `@PreAuthorize("hasRole('ADMIN')")` on admin controllers
- Roles stored as `ROLE_USER` / `ROLE_ADMIN` for Spring Security

## Requirement → Component Mapping

| Req ID | Component | Mechanism |
|--------|-----------|-----------|
| FR-001 | AuthController → UserService | POST /auth/register |
| FR-002 | UserService → PasswordEncoder | BCrypt bean |
| FR-003 | AuthController → AuthService | POST /auth/login |
| FR-004 | JwtTokenProvider | generateToken() |
| FR-005 | JwtTokenProvider.generateToken() | Claims builder |
| FR-006 | JwtAuthenticationFilter | Filter chain |
| FR-007 | Role entity + UserDetails | user_roles join |
| FR-008 | AdminUserController | GET /admin/users |
| FR-009 | AdminUserController → UserService | PUT /admin/users/{id}/status |
| FR-010 | JwtAuthenticationFilter | 401 response |
| FR-011 | Spring Security AccessDeniedException | @PreAuthorize |
| FR-012 | AuthController | GET /auth/me |
| FR-013 | UserRepository → PostgreSQL | default_schema=identity |
