package com.secureclaims.claims.exception;

/**
 * Thrown when an invalid claim status transition is attempted.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(final String message) {
        super(message);
    }
}
