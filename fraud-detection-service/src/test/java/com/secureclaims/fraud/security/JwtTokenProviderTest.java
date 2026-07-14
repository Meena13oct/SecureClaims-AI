package com.secureclaims.fraud.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
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

    private static final String SECRET = "test-secret-key-for-unit-tests-must-be-at-least-256-bits-long-2026";
    private JwtTokenProvider jwtTokenProvider;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET);
        secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void should_returnTrue_when_tokenIsValid() {
        // given
        final String token = buildToken(UUID.randomUUID(), List.of("ROLE_USER"), 60000);

        // when
        final boolean result = jwtTokenProvider.validateToken(token);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void should_returnFalse_when_tokenIsExpired() {
        // given
        final String token = buildToken(UUID.randomUUID(), List.of("ROLE_USER"), -1000);

        // when
        final boolean result = jwtTokenProvider.validateToken(token);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void should_returnFalse_when_tokenIsMalformed() {
        // when
        final boolean result = jwtTokenProvider.validateToken("not-a-valid-token");

        // then
        assertThat(result).isFalse();
    }

    @Test
    void should_extractUserId_when_tokenIsValid() {
        // given
        final UUID userId = UUID.randomUUID();
        final String token = buildToken(userId, List.of("ROLE_USER"), 60000);

        // when
        final UUID extractedId = jwtTokenProvider.getUserIdFromToken(token);

        // then
        assertThat(extractedId).isEqualTo(userId);
    }

    @Test
    void should_extractRoles_when_tokenIsValid() {
        // given
        final List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");
        final String token = buildToken(UUID.randomUUID(), roles, 60000);

        // when
        final List<String> extractedRoles = jwtTokenProvider.getRolesFromToken(token);

        // then
        assertThat(extractedRoles).containsExactlyInAnyOrderElementsOf(roles);
    }

    @Test
    void should_returnFalse_when_tokenSignedWithDifferentKey() {
        // given
        final SecretKey differentKey = Keys.hmacShaKeyFor(
                "different-secret-key-that-is-long-enough-for-hmac-sha-256-algorithm".getBytes(StandardCharsets.UTF_8));
        final String token = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("roles", List.of("ROLE_USER"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(differentKey)
                .compact();

        // when
        final boolean result = jwtTokenProvider.validateToken(token);

        // then
        assertThat(result).isFalse();
    }

    private String buildToken(final UUID userId, final List<String> roles, final long expiryMs) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(secretKey)
                .compact();
    }
}
