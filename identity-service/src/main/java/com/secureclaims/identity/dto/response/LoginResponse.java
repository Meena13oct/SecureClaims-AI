package com.secureclaims.identity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO containing the JWT token after successful login.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Data
@Builder
public class LoginResponse {

    @Schema(description = "JWT authentication token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
}
