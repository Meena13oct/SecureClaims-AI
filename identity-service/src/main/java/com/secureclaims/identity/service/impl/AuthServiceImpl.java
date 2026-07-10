package com.secureclaims.identity.service.impl;

import com.secureclaims.identity.dto.request.LoginRequest;
import com.secureclaims.identity.dto.request.RegisterRequest;
import com.secureclaims.identity.dto.response.LoginResponse;
import com.secureclaims.identity.dto.response.UserResponse;
import com.secureclaims.identity.entity.Role;
import com.secureclaims.identity.entity.User;
import com.secureclaims.identity.exception.DuplicateResourceException;
import com.secureclaims.identity.exception.InvalidCredentialsException;
import com.secureclaims.identity.exception.ResourceNotFoundException;
import com.secureclaims.identity.repository.RoleRepository;
import com.secureclaims.identity.repository.UserRepository;
import com.secureclaims.identity.security.JwtTokenProvider;
import com.secureclaims.identity.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of authentication and user management operations.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String ROLE_USER = "USER";
    private static final String ROLE_ADMIN = "ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Register a new user account.
     * The first registered user is assigned the ADMIN role; all subsequent users receive the USER role.
     *
     * @param request the registration request containing user details
     * @return the created user's response details
     * @throws DuplicateResourceException if email or username already exists
     */
    @Override
    @Transactional
    public UserResponse register(final RegisterRequest request) {

        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use");
        }

        // Check for duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already in use");
        }

        // Determine role: first user gets ADMIN, subsequent users get USER
        final boolean isFirstUser = userRepository.count() == 0;
        final String roleName = isFirstUser ? ROLE_ADMIN : ROLE_USER;

        // Fetch the appropriate role
        final Role assignedRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));

        // Build and save the user entity
        final User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(assignedRole));

        final User savedUser = userRepository.save(user);
        log.info("User registered: userId={}, username={}, role={}", savedUser.getId(), savedUser.getUsername(), roleName);

        return mapToUserResponse(savedUser);
    }

    /**
     * Authenticate a user and issue a JWT token.
     *
     * @param request the login request containing email and password
     * @return the login response containing the signed JWT token
     * @throws InvalidCredentialsException if email not found or password does not match
     */
    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(final LoginRequest request) {

        // Look up user by email
        final User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Verify password using BCrypt matches
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Build role list for JWT claims
        final List<String> roles = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .collect(Collectors.toList());

        // Generate JWT token with full user profile for /auth/me resolution
        final String token = jwtTokenProvider.generateToken(
                user.getId(), user.getUsername(), user.getEmail(),
                user.getFirstName(), user.getLastName(), roles);
        log.info("User logged in: userId={}, username={}", user.getId(), user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .build();
    }

    private UserResponse mapToUserResponse(final User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .build();
    }
}
