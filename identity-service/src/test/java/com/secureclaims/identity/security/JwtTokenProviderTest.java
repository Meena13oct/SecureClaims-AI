package com.secureclaims.identity.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtTokenProvider.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
class JwtTokenProviderTest {

    private static final String SECRET = "my-super-secret-key-that-is-at-least-32-characters-long";
    private static final long EXPIRY_MS = 86400000L; // 24 hours

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRY_MS);
    }

    @Test
    void should_generateValidToken_when_allFieldsProvided() {
        // given
        final UUID userId = UUID.randomUUID();

        // when
        final String token = jwtTokenProvider.generateToken(
                userId, "janedoe", "jane@example.com", "Jane", "Doe", List.of("ROLE_USER"));

        // then
        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void should_extractUserId_when_tokenIsValid() {
        // given
        final UUID userId = UUID.randomUUID();
        final String token = jwtTokenProvider.generateToken(
                userId, "janedoe", "jane@example.com", "Jane", "Doe", List.of("ROLE_USER"));

        // when
        final UUID extractedId = jwtTokenProvider.getUserIdFromToken(token);

        // then
        assertThat(extractedId).isEqualTo(userId);
    }

    @Test
    void should_extractUsername_when_tokenIsValid() {
        // given
        final UUID userId = UUID.randomUUID();
        final String token = jwtTokenProvider.generateToken(
                userId, "janedoe", "jane@example.com", "Jane", "Doe", List.of("ROLE_USER"));

        // when
        final String username = jwtTokenProvider.getUsernameFromToken(token);

        // then
        assertThat(username).isEqualTo("janedoe");
    }

    @Test
    void should_extractEmail_when_tokenIsValid() {
        // given
        final UUID userId = UUID.randomUUID();
        final String token = jwtTokenProvider.generateToken(
                userId, "janedoe", "jane@example.com", "Jane", "Doe", List.of("ROLE_USER"));

        // when
        final String email = jwtTokenProvider.getEmailFromToken(token);

        // then
        assertThat(email).isEqualTo("jane@example.com");
    }

    @Test
    void should_extractFirstName_when_tokenIsValid() {
        // given
        final UUID userId = UUID.randomUUID();
        final String token = jwtTokenProvider.generateToken(
                userId, "janedoe", "jane@example.com", "Jane", "Doe", List.of("ROLE_USER"));

        // when
        final String firstName = jwtTokenProvider.getFirstNameFromToken(token);

        // then
        assertThat(firstName).isEqualTo("Jane");
    }

    @Test
    void should_extractLastName_when_tokenIsValid() {
        // given
        final UUID userId = UUID.randomUUID();
        final String token = jwtTokenProvider.generateToken(
                userId, "janedoe", "jane@example.com", "Jane", "Doe", List.of("ROLE_USER"));

        // when
        final String lastName = jwtTokenProvider.getLastNameFromToken(token);

        // then
        assertThat(lastName).isEqualTo("Doe");
    }

    @Test
    void should_extractRoles_when_tokenIsValid() {
        // given
        final UUID userId = UUID.randomUUID();
        final String token = jwtTokenProvider.generateToken(
                userId, "admin", "admin@example.com", "Admin", "User", List.of("ROLE_USER", "ROLE_ADMIN"));

        // when
        final List<String> roles = jwtTokenProvider.getRolesFromToken(token);

        // then
        assertThat(roles).containsExactly("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void should_returnFalse_when_tokenIsInvalid() {
        // when
        final boolean valid = jwtTokenProvider.validateToken("invalid.token.string");

        // then
        assertThat(valid).isFalse();
    }

    @Test
    void should_returnFalse_when_tokenIsExpired() {
        // given — token with 0ms expiry (immediately expired)
        final JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, 0L);
        final String token = expiredProvider.generateToken(
                UUID.randomUUID(), "test", "test@test.com", "Test", "User", List.of("ROLE_USER"));

        // when — slight delay to ensure expiry
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        final boolean valid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(valid).isFalse();
    }

    @Test
    void should_generateToken_when_usingBackwardCompatibleOverload() {
        // given
        final UUID userId = UUID.randomUUID();

        // when
        final String token = jwtTokenProvider.generateToken(userId, "janedoe", List.of("ROLE_USER"));

        // then
        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("janedoe");
        assertThat(jwtTokenProvider.getEmailFromToken(token)).isNull();
    }
}
