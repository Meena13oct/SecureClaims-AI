package com.secureclaims.notification.exception;

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
        final BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "req");
        bindingResult.addError(new FieldError("req", "userId", "must not be null"));
        final MethodParameter param = new MethodParameter(
                this.getClass().getDeclaredMethod("should_return400_when_validationFails"), -1);
        final MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindingResult);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/notifications/v1/admin/notifications");

        // when
        final ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
        assertThat(response.getBody()).containsKey("fieldErrors");
    }

    @Test
    void should_return403_when_accessDenied() {
        // given
        final AccessDeniedException ex = new AccessDeniedException("Forbidden");
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/notifications/v1/admin/notifications/123");

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
        final Exception ex = new RuntimeException("Unexpected error");
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/notifications/v1/test");

        // when
        final ResponseEntity<Map<String, Object>> response = handler.handleGeneric(ex, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("status", 500);
        assertThat(response.getBody()).containsEntry("message", "An unexpected error occurred");
    }
}
