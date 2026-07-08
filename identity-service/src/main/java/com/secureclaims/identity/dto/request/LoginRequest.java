package com.secureclaims.identity.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for user login.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Data
public class LoginRequest {

    @Schema(description = "User's registered email address", example = "jane.doe@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @Schema(description = "User's password", example = "SecureP@ss1")
    @NotBlank(message = "Password is required")
    private String password;
}
