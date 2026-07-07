package com.secureclaims.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Identity Service microservice.
 * Handles user registration, authentication, and JWT token issuance.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@SpringBootApplication
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
