package com.secureclaims.identity.exception;

/**
 * Thrown when a requested resource is not found in the system.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(final String resource, final String field, final Object value) {
        super(String.format("%s not found with %s: '%s'", resource, field, value));
    }
}
