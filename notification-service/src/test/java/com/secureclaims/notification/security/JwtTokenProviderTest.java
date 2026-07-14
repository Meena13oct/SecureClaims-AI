package com.secureclaims.notification.security;

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
        final String token = buildToken(UUID.randomUUID(), List.of("ROLE_USER"), 60000);
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void should_returnFalse_when_tokenIsExpired() {
        final String token = buildToken(UUID.randomUUID(), List.of("ROLE_USER"), -1000);
        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    void should_returnFalse_when_tokenIsMalformed() {
        assertThat(jwtTokenProvider.validateToken("garbage-token")).isFalse();
    }

    @Test
    void should_extractUserId_when_tokenIsValid() {
        final UUID userId = UUID.randomUUID();
        final String token = buildToken(userId, List.of("ROLE_USER"), 60000);
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(userId);
    }

    @Test
    void should_extractRoles_when_tokenIsValid() {
        final List<String> roles = List.of("ROLE_ADMIN");
        final String token = buildToken(UUID.randomUUID(), roles, 60000);
        assertThat(jwtTokenProvider.getRolesFromToken(token)).containsExactly("ROLE_ADMIN");
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
