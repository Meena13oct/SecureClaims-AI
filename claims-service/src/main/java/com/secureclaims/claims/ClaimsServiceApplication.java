package com.secureclaims.claims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Claims Service microservice.
 * Manages insurance claim submissions, status lifecycle, and document uploads.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@SpringBootApplication
public class ClaimsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClaimsServiceApplication.class, args);
    }
}
