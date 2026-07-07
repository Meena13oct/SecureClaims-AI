package com.secureclaims.fraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Fraud Detection Service microservice.
 * Performs rule-based fraud risk scoring on submitted insurance claims.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@SpringBootApplication
public class FraudDetectionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudDetectionServiceApplication.class, args);
    }
}
