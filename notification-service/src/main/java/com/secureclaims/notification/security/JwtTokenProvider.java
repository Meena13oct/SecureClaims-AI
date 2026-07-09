package com.secureclaims.notification.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {
    private final SecretKey secretKey;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public UUID getUserIdFromToken(String token) { return UUID.fromString(parseClaims(token).getSubject()); }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) { return parseClaims(token).get("roles", List.class); }

    public boolean validateToken(String token) {
        try { parseClaims(token); return true; } catch (Exception ex) { log.warn("Invalid JWT: {}", ex.getMessage()); return false; }
    }

    private Claims parseClaims(String token) { return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload(); }
}
