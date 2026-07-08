package com.secureclaims.identity.service;

import com.secureclaims.identity.dto.request.LoginRequest;
import com.secureclaims.identity.dto.request.RegisterRequest;
import com.secureclaims.identity.dto.response.LoginResponse;
import com.secureclaims.identity.dto.response.UserResponse;

/**
 * Service interface for authentication and user management operations.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
public interface AuthService {

    /**
     * Register a new user account.
     *
     * @param request the registration request containing user details
     * @return the created user's details
     * @throws com.secureclaims.identity.exception.DuplicateResourceException if email or username already exists
     */
    UserResponse register(RegisterRequest request);

    /**
     * Authenticate a user with email and password, returning a JWT token.
     *
     * @param request the login request containing email and password
     * @return the login response containing the JWT token
     * @throws com.secureclaims.identity.exception.InvalidCredentialsException if email or password is wrong
     */
    LoginResponse login(LoginRequest request);
}
