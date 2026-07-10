package com.secureclaims.identity.service.impl;

import com.secureclaims.identity.dto.request.LoginRequest;
import com.secureclaims.identity.dto.request.RegisterRequest;
import com.secureclaims.identity.dto.response.LoginResponse;
import com.secureclaims.identity.dto.response.UserResponse;
import com.secureclaims.identity.entity.Role;
import com.secureclaims.identity.entity.User;
import com.secureclaims.identity.exception.DuplicateResourceException;
import com.secureclaims.identity.exception.InvalidCredentialsException;
import com.secureclaims.identity.repository.RoleRepository;
import com.secureclaims.identity.repository.UserRepository;
import com.secureclaims.identity.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthServiceImpl.
 * Covers US-003 (register) and US-004 (login).
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void should_registerUser_when_validRequest() {
        // given
        final RegisterRequest request = buildRegisterRequest();
        final Role userRole = buildRole("USER");
        final User savedUser = buildUser(userRole);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("$2a$10$encodedHash");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        final UserResponse response = authService.register(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("janedoe");
        assertThat(response.getEmail()).isEqualTo("jane.doe@example.com");
        assertThat(response.getRoles()).contains("USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void should_throwDuplicateResource_when_emailAlreadyExists() {
        // given
        final RegisterRequest request = buildRegisterRequest();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // when/then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void should_throwDuplicateResource_when_usernameAlreadyExists() {
        // given
        final RegisterRequest request = buildRegisterRequest();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        // when/then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username already in use");
    }

    @Test
    void should_loginSuccessfully_when_validCredentials() {
        // given
        final LoginRequest request = new LoginRequest();
        request.setEmail("jane.doe@example.com");
        request.setPassword("SecureP@ss1");

        final Role userRole = buildRole("USER");
        final User user = buildUser(userRole);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(UUID.class), anyString(), anyString(),
                anyString(), anyString(), anyList())).thenReturn("mock.jwt.token");

        // when
        final LoginResponse response = authService.login(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
    }

    @Test
    void should_throwInvalidCredentials_when_emailNotFound() {
        // given
        final LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("AnyPassword1");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void should_throwInvalidCredentials_when_passwordDoesNotMatch() {
        // given
        final LoginRequest request = new LoginRequest();
        request.setEmail("jane.doe@example.com");
        request.setPassword("WrongPassword!");

        final Role userRole = buildRole("USER");
        final User user = buildUser(userRole);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(false);

        // when/then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    private RegisterRequest buildRegisterRequest() {
        final RegisterRequest request = new RegisterRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane.doe@example.com");
        request.setUsername("janedoe");
        request.setPassword("SecureP@ss1");
        return request;
    }

    private Role buildRole(final String name) {
        final Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName(name);
        return role;
    }

    private User buildUser(final Role role) {
        final User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane.doe@example.com");
        user.setUsername("janedoe");
        user.setPasswordHash("$2a$10$encodedHash");
        user.setRoles(Set.of(role));
        return user;
    }
}
