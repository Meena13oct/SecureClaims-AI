package com.secureclaims.claims.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for submitting a new claim.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Data
public class ClaimRequest {

    @Schema(description = "Policy number", example = "POL-2026-00123")
    @NotBlank(message = "Policy number is required")
    private String policyNumber;

    @Schema(description = "Type of claim", example = "MEDICAL")
    @NotBlank(message = "Claim type is required")
    private String claimType;

    @Schema(description = "Date of incident", example = "2026-06-20")
    @NotNull(message = "Incident date is required")
    private LocalDate incidentDate;

    @Schema(description = "Description of the claim", example = "Hospitalisation due to surgery")
    @NotBlank(message = "Description is required")
    private String description;

    @Schema(description = "Amount claimed in dollars", example = "75000.00")
    @NotNull(message = "Claimed amount is required")
    @Min(value = 1, message = "Claimed amount must be at least 1")
    private BigDecimal claimedAmount;

    @Schema(description = "Policy age in months", example = "12")
    private int policyAgeMonths = 12;
}
