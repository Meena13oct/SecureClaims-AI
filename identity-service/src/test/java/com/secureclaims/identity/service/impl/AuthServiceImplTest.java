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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthServiceImpl.
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

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Role userRole;
    private User existingUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("jane.doe@example.com");
        registerRequest.setUsername("janedoe");
        registerRequest.setPassword("SecureP@ss1");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("jane.doe@example.com");
        loginRequest.setPassword("SecureP@ss1");

        userRole = new Role("USER");
        userRole.setId(UUID.randomUUID());

        existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setUsername("janedoe");
        existingUser.setEmail("jane.doe@example.com");
        existingUser.setFirstName("Jane");
        existingUser.setLastName("Doe");
        existingUser.setPasswordHash("$2a$10$hashedPassword");
        existingUser.setRoles(Set.of(userRole));
    }

    // ========== Registration Tests ==========

    @Test
    void should_registerUser_when_validRequest() {
        // given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            final User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        // when
        final UserResponse response = authService.register(registerRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isNotNull();
        assertThat(response.getUsername()).isEqualTo("janedoe");
        assertThat(response.getEmail()).isEqualTo("jane.doe@example.com");
        assertThat(response.getFirstName()).isEqualTo("Jane");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getRoles()).containsExactly("USER");
    }

    @Test
    void should_hashPassword_when_registeringUser() {
        // given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("SecureP@ss1")).thenReturn("$2a$10$encodedHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            final User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        // when
        authService.register(registerRequest);

        // then
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("$2a$10$encodedHash");
    }

    @Test
    void should_throwDuplicateResourceException_when_emailAlreadyExists() {
        // given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already in use");

        verify(userRepository, never()).save(any());
    }

    @Test
    void should_throwDuplicateResourceException_when_usernameAlreadyExists() {
        // given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Username already in use");

        verify(userRepository, never()).save(any());
    }

    @Test
    void should_throwResourceNotFoundException_when_userRoleNotFound() {
        // given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Role not found with name: 'USER'");

        verify(userRepository, never()).save(any());
    }

    @Test
    void should_assignUserRole_when_registering() {
        // given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            final User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        // when
        final UserResponse response = authService.register(registerRequest);

        // then
        assertThat(response.getRoles()).containsExactly("USER");
    }

    // ========== Login Tests ==========

    @Test
    void should_returnToken_when_validCredentials() {
        // given
        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("SecureP@ss1", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(eq(existingUser.getId()), eq("janedoe"), anyList()))
                .thenReturn("mock.jwt.token");

        // when
        final LoginResponse response = authService.login(loginRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
    }

    @Test
    void should_generateTokenWithRoles_when_loginSuccessful() {
        // given
        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("SecureP@ss1", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(UUID.class), anyString(), anyList()))
                .thenReturn("mock.jwt.token");

        // when
        authService.login(loginRequest);

        // then — verify JWT is generated with ROLE_ prefix
        @SuppressWarnings("unchecked")
        final ArgumentCaptor<List<String>> rolesCaptor = ArgumentCaptor.forClass(List.class);
        verify(jwtTokenProvider).generateToken(eq(existingUser.getId()), eq("janedoe"), rolesCaptor.capture());
        assertThat(rolesCaptor.getValue()).containsExactly("ROLE_USER");
    }

    @Test
    void should_throwInvalidCredentialsException_when_emailNotFound() {
        // given
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        loginRequest.setEmail("unknown@example.com");

        // when / then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void should_throwInvalidCredentialsException_when_passwordDoesNotMatch() {
        // given
        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("SecureP@ss1", "$2a$10$hashedPassword")).thenReturn(false);

        // when / then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(jwtTokenProvider, never()).generateToken(any(), anyString(), anyList());
    }

    @Test
    void should_useBcryptMatches_when_verifyingPassword() {
        // given
        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("SecureP@ss1", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(UUID.class), anyString(), anyList()))
                .thenReturn("token");

        // when
        authService.login(loginRequest);

        // then — verify BCrypt matches() is used
        verify(passwordEncoder).matches("SecureP@ss1", "$2a$10$hashedPassword");
    }
}
