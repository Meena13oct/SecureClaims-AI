# Identity Management Service – Tasks

## US-003: User Registration
- [x] Create `RegisterRequest` DTO with validation (@NotBlank, @Email, @Size)
- [x] Create `UserResponse` DTO (excludes password)
- [x] Implement `AuthService.register()` — validate uniqueness, hash password, assign USER role
- [x] Implement `POST /auth/register` in AuthController
- [x] Return 409 Conflict for duplicate email/username
- [x] Return 201 Created with user details (no password)
- [x] Unit test: register success, duplicate email error

## US-004: User Login and JWT Issuance
- [x] Create `LoginRequest` DTO with validation
- [x] Create `JwtTokenProvider` — generate token with userId, username, roles, expiry
- [x] Implement `AuthService.login()` — verify credentials with BCrypt
- [x] Implement `POST /auth/login` in AuthController
- [x] JWT signed with HMAC-SHA256, 24-hour expiry from app.jwt.secret
- [x] Return 401 Unauthorized for wrong credentials
- [x] Unit test: login success, wrong password returns 401

## US-005: JWT Authentication Filter & Security Config
- [x] Create `JwtAuthenticationFilter` extending `OncePerRequestFilter`
- [x] Read `Authorization: Bearer <token>` header
- [x] Validate token and populate SecurityContextHolder
- [x] Configure `SecurityConfig` — permit /auth/**, secure everything else
- [x] Set `SessionCreationPolicy.STATELESS`
- [x] Register filter before UsernamePasswordAuthenticationFilter
- [x] Return 401 for missing/invalid tokens

## US-006: Role-Based Access Control & User Profile
- [x] Implement `GET /auth/me` — resolve user from JWT, no DB call
- [x] Apply `@PreAuthorize("hasRole('ADMIN')")` on admin controllers
- [x] USER role grants access to /claims/** and GET /auth/me
- [x] ADMIN role grants access to /admin/**
- [x] Return 403 Forbidden for USER accessing admin routes
- [x] Embed roles as ROLE_USER / ROLE_ADMIN in JWT

## US-016: Admin: List All Users
- [x] Implement `GET /admin/users` with pagination
- [x] Return userId, username, email, roles for each user
- [x] Require ADMIN JWT — return 403 for USER tokens

## US-017: Global Exception Handler (Identity Service)
- [x] Create `GlobalExceptionHandler` with @RestControllerAdvice
- [x] Handle MethodArgumentNotValidException → 400
- [x] Handle ResourceNotFoundException → 404
- [x] Handle AccessDeniedException → 403
- [x] Handle generic Exception → 500 (no stack trace)
- [x] Standard error shape: timestamp, status, error, message, path

## US-018: Swagger UI
- [x] Add springdoc-openapi-starter-webmvc-ui dependency
- [x] Configure JWT Bearer security scheme
- [x] Add @Operation annotations on all controller methods
- [x] Swagger UI accessible at http://localhost:8081/swagger-ui.html
