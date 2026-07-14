package com.secureclaims.identity.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GlobalExceptionHandler.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void should_return400_when_validationFails() throws NoSuchMethodException {
        // given
        final BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "must not be blank"));
        final MethodParameter param = new MethodParameter(
                this.getClass().getDeclaredMethod("should_return400_when_validationFails"), -1);
        final MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindingResult);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/identity/v1/auth/register");

        // when
        final ResponseEntity<Map<String, Object>> response = handler.handleValidationException(ex, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
        assertThat(response.getBody()).containsKey("fieldErrors");
    }

    @Test
    void should_return409_when_duplicateResource() {
        // given
        final DuplicateResourceException ex = new DuplicateResourceException("Email already exists");
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/identity/v1/auth/register");

        // when
        final ResponseEntity<Map<String, Object>> response = handler.handleDuplicateResource(ex, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("status", 409);
        assertThat(response.getBody().get("message").toString()).contains("Email already exists");
    }

    @Test
    void should_return401_when_invalidCredentials() {
        // given
        final InvalidCredentialsException ex = new InvalidCredentialsException("Invalid email or password");
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/identity/v1/auth/login");

        // when
        final ResponseEntity<Map<String, Object>> response = handler.handleInvalidCredentials(ex, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("status", 401);
    }

    @Test
    void should_return404_when_resourceNotFound() {
        // given
        final ResourceNotFoundException ex = new ResourceNotFoundException("User", "id", "abc-123");
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/identity/v1/admin/users/abc-123");

        // when
        final ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFound(ex, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("status", 404);
        assertThat(response.getBody().get("message").toString()).contains("User");
    }

    @Test
    void should_return403_when_accessDenied() {
        // given
        final AccessDeniedException ex = new AccessDeniedException("Not allowed");
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/identity/v1/admin/users");

        // when
        final ResponseEntity<Map<String, Object>> response = handler.handleAccessDenied(ex, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).containsEntry("status", 403);
        assertThat(response.getBody()).containsEntry("message", "Access denied");
    }

    @Test
    void should_return500_when_genericException() {
        // given
        final Exception ex = new RuntimeException("Unexpected failure");
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/identity/v1/auth/login");

        // when
        final ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("status", 500);
        assertThat(response.getBody()).containsEntry("message", "An unexpected error occurred");
    }
}
