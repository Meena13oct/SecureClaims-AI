package com.secureclaims.identity.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for user registration.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Data
public class RegisterRequest {

    @Schema(description = "User's first name", example = "Jane")
    @NotBlank(message = "First name is required")
    private String firstName;

    @Schema(description = "User's last name", example = "Doe")
    @NotBlank(message = "Last name is required")
    private String lastName;

    @Schema(description = "User's email address", example = "jane.doe@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @Schema(description = "Unique username", example = "janedoe")
    @NotBlank(message = "Username is required")
    private String username;

    @Schema(description = "Password (minimum 8 characters)", example = "SecureP@ss1")
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
