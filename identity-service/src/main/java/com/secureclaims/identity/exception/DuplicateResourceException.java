package com.secureclaims.identity.exception;

/**
 * Thrown when attempting to create a resource that already exists.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(final String message) {
        super(message);
    }
}
