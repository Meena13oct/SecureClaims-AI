package com.secureclaims.claims.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for claim details.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Data
@Builder
public class ClaimResponse {

    @Schema(description = "Claim ID")
    private UUID claimId;

    @Schema(description = "Owner user ID")
    private UUID userId;

    @Schema(description = "Policy number")
    private String policyNumber;

    @Schema(description = "Claim type")
    private String claimType;

    @Schema(description = "Incident date")
    private LocalDate incidentDate;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Claimed amount")
    private BigDecimal claimedAmount;

    @Schema(description = "Current status")
    private String status;

    @Schema(description = "Submission timestamp")
    private LocalDateTime submittedAt;

    @Schema(description = "Last updated timestamp")
    private LocalDateTime updatedAt;
}
