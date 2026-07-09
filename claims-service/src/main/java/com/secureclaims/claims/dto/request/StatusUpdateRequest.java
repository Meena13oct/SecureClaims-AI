package com.secureclaims.claims.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for updating claim status.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Data
public class StatusUpdateRequest {

    @Schema(description = "New status", example = "UNDER_REVIEW")
    @NotBlank(message = "Status is required")
    private String status;
}
