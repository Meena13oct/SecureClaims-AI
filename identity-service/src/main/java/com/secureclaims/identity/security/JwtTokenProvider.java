package com.secureclaims.identity.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for creating and validating JWT tokens.
 * Tokens contain userId, username, email, firstName, lastName, and roles.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expiryMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") final String secret,
            @Value("${app.jwt.expiry-ms}") final long expiryMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMs = expiryMs;
    }

    /**
     * Generate a JWT token for the given user details.
     *
     * @param userId    the user's UUID
     * @param username  the user's username
     * @param email     the user's email
     * @param firstName the user's first name
     * @param lastName  the user's last name
     * @param roles     the user's roles (e.g., ROLE_USER, ROLE_ADMIN)
     * @return the signed JWT token string
     */
    public String generateToken(final UUID userId, final String username, final String email,
                                final String firstName, final String lastName, final List<String> roles) {
        final Date now = new Date();
        final Date expiryDate = new Date(now.getTime() + expiryMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("email", email)
                .claim("firstName", firstName)
                .claim("lastName", lastName)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generate a JWT token (backward-compatible overload without profile fields).
     *
     * @param userId   the user's UUID
     * @param username the user's username
     * @param roles    the user's roles
     * @return the signed JWT token string
     */
    public String generateToken(final UUID userId, final String username, final List<String> roles) {
        return generateToken(userId, username, null, null, null, roles);
    }

    /**
     * Extract the user ID (subject) from a JWT token.
     *
     * @param token the JWT token
     * @return the user UUID from the token subject
     */
    public UUID getUserIdFromToken(final String token) {
        final Claims claims = parseClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    /**
     * Extract the username from a JWT token.
     *
     * @param token the JWT token
     * @return the username
     */
    public String getUsernameFromToken(final String token) {
        final Claims claims = parseClaims(token);
        return claims.get("username", String.class);
    }

    /**
     * Extract email from a JWT token.
     *
     * @param token the JWT token
     * @return the email address, or null if not present
     */
    public String getEmailFromToken(final String token) {
        final Claims claims = parseClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * Extract first name from a JWT token.
     *
     * @param token the JWT token
     * @return the first name, or null if not present
     */
    public String getFirstNameFromToken(final String token) {
        final Claims claims = parseClaims(token);
        return claims.get("firstName", String.class);
    }

    /**
     * Extract last name from a JWT token.
     *
     * @param token the JWT token
     * @return the last name, or null if not present
     */
    public String getLastNameFromToken(final String token) {
        final Claims claims = parseClaims(token);
        return claims.get("lastName", String.class);
    }

    /**
     * Extract roles from a JWT token.
     *
     * @param token the JWT token
     * @return list of role names
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(final String token) {
        final Claims claims = parseClaims(token);
        return claims.get("roles", List.class);
    }

    /**
     * Validate whether a JWT token is valid and not expired.
     *
     * @param token the JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(final String token) {
        try {
            parseClaims(token);
            return true;
        } catch (final Exception ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    private Claims parseClaims(final String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
