package com.secureclaims.identity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for user details (excludes sensitive fields like password).
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Data
@Builder
public class UserResponse {

    @Schema(description = "Unique user identifier")
    private UUID userId;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "User's email address")
    private String email;

    @Schema(description = "User's first name")
    private String firstName;

    @Schema(description = "User's last name")
    private String lastName;

    @Schema(description = "List of assigned roles")
    private List<String> roles;
}
