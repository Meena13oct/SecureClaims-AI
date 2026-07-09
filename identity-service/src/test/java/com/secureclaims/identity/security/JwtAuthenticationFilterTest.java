package com.secureclaims.identity.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JwtAuthenticationFilter.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_continueFilterChain_when_noAuthorizationHeader() throws Exception {
        // given
        when(request.getHeader("Authorization")).thenReturn(null);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void should_continueFilterChain_when_authHeaderNotBearer() throws Exception {
        // given
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void should_return401_when_tokenIsInvalid() throws Exception {
        // given
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token.here");
        when(request.getRequestURI()).thenReturn("/api/identity/v1/auth/me");
        when(jwtTokenProvider.validateToken("invalid.token.here")).thenReturn(false);

        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);

        printWriter.flush();
        assertThat(stringWriter.toString()).contains("\"status\":401");
        assertThat(stringWriter.toString()).contains("Invalid or expired JWT token");
    }

    @Test
    void should_setAuthentication_when_tokenIsValid() throws Exception {
        // given
        final String token = "valid.jwt.token";
        final UUID userId = UUID.randomUUID();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn("janedoe");
        when(jwtTokenProvider.getRolesFromToken(token)).thenReturn(List.of("ROLE_USER"));
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(userId.toString());
        assertThat(auth.getAuthorities()).hasSize(1);
        assertThat(auth.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void should_setMultipleAuthorities_when_tokenHasMultipleRoles() throws Exception {
        // given
        final String token = "admin.jwt.token";
        final UUID userId = UUID.randomUUID();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn("admin");
        when(jwtTokenProvider.getRolesFromToken(token)).thenReturn(List.of("ROLE_USER", "ROLE_ADMIN"));
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).hasSize(2);
    }
}
