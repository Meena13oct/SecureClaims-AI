package com.secureclaims.identity.exception;

/**
 * Thrown when login credentials (email or password) are invalid.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(final String message) {
        super(message);
    }
}
