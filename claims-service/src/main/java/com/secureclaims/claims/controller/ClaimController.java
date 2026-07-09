package com.secureclaims.claims.controller;

import com.secureclaims.claims.dto.request.ClaimRequest;
import com.secureclaims.claims.dto.request.StatusUpdateRequest;
import com.secureclaims.claims.dto.response.ApiResponse;
import com.secureclaims.claims.dto.response.ClaimResponse;
import com.secureclaims.claims.dto.response.DocumentResponse;
import com.secureclaims.claims.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for claim operations.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/claims/v1")
@RequiredArgsConstructor
@Tag(name = "Claims", description = "Claim submission, viewing, and management endpoints")
public class ClaimController {

    private final ClaimService claimService;

    @Operation(summary = "Submit a new claim", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/claims")
    public ResponseEntity<ApiResponse<ClaimResponse>> createClaim(
            @Valid @RequestBody final ClaimRequest request, final Authentication auth) {

        final UUID userId = UUID.fromString(auth.getName());
        final ClaimResponse response = claimService.createClaim(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Claim submitted successfully", response));
    }

    @Operation(summary = "List my claims", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/claims")
    public ResponseEntity<ApiResponse<Page<ClaimResponse>>> getMyClaims(final Authentication auth, final Pageable pageable) {

        final UUID userId = UUID.fromString(auth.getName());
        final Page<ClaimResponse> claims = claimService.getClaimsByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, "Claims retrieved", claims));
    }

    @Operation(summary = "Get a specific claim", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/claims/{id}")
    public ResponseEntity<ApiResponse<ClaimResponse>> getClaimById(
            @PathVariable final UUID id, final Authentication auth) {

        final UUID userId = UUID.fromString(auth.getName());
        final ClaimResponse response = claimService.getClaimById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(200, "Claim retrieved", response));
    }

    @Operation(summary = "Admin: List all claims", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/admin/claims")
    public ResponseEntity<ApiResponse<Page<ClaimResponse>>> getAllClaims(final Pageable pageable) {

        final Page<ClaimResponse> claims = claimService.getAllClaims(pageable);
        return ResponseEntity.ok(ApiResponse.success(200, "All claims retrieved", claims));
    }

    @Operation(summary = "Admin: Update claim status", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/admin/claims/{id}/status")
    public ResponseEntity<ApiResponse<ClaimResponse>> updateStatus(
            @PathVariable final UUID id, @Valid @RequestBody final StatusUpdateRequest request) {

        final ClaimResponse response = claimService.updateClaimStatus(id, request.getStatus(), "ADMIN");
        return ResponseEntity.ok(ApiResponse.success(200, "Claim status updated", response));
    }

    @Operation(summary = "Upload a PDF document to a claim", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/claims/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @PathVariable final UUID id, @RequestParam("file") final MultipartFile file, final Authentication auth) {

        final UUID userId = UUID.fromString(auth.getName());
        final DocumentResponse response = claimService.uploadDocument(id, userId, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Document uploaded successfully", response));
    }

    @Operation(summary = "List documents for a claim", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/claims/{id}/documents")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocuments(
            @PathVariable final UUID id, final Authentication auth) {

        final UUID userId = UUID.fromString(auth.getName());
        final List<DocumentResponse> docs = claimService.getDocuments(id, userId);
        return ResponseEntity.ok(ApiResponse.success(200, "Documents retrieved", docs));
    }
}
