package com.secureclaims.identity.controller;

import com.secureclaims.identity.dto.request.LoginRequest;
import com.secureclaims.identity.dto.request.RegisterRequest;
import com.secureclaims.identity.dto.response.ApiResponse;
import com.secureclaims.identity.dto.response.LoginResponse;
import com.secureclaims.identity.dto.response.UserResponse;
import com.secureclaims.identity.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling authentication operations (registration, login).
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/identity/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user account.
     *
     * @param request the registration details
     * @return the created user information
     */
    @Operation(summary = "Register a new user account")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failure"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email or username already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody final RegisterRequest request) {

        log.info("Registration request received for username={}", request.getUsername());
        final UserResponse userResponse = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "User registered successfully", userResponse));
    }

    /**
     * Authenticate a user and issue a JWT token.
     *
     * @param request the login credentials (email and password)
     * @return the JWT token
     */
    @Operation(summary = "Login and receive a JWT token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failure"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody final LoginRequest request) {

        log.info("Login request received for email={}", request.getEmail());
        final LoginResponse loginResponse = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Login successful", loginResponse));
    }
}
