package com.secureclaims.identity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test to verify the Identity Service application context loads successfully.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@SpringBootTest
@ActiveProfiles("test")
class IdentityServiceApplicationTests {

    @Test
    void should_loadContext_when_applicationStarts() {
        // Verifies that the Spring application context loads without errors
    }
}
