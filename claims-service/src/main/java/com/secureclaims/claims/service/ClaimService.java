package com.secureclaims.claims.service;

import com.secureclaims.claims.dto.request.ClaimRequest;
import com.secureclaims.claims.dto.response.ClaimResponse;
import com.secureclaims.claims.dto.response.DocumentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for claim operations.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
public interface ClaimService {

    ClaimResponse createClaim(ClaimRequest request, UUID userId);

    Page<ClaimResponse> getClaimsByUser(UUID userId, Pageable pageable);

    ClaimResponse getClaimById(UUID claimId, UUID userId);

    Page<ClaimResponse> getAllClaims(Pageable pageable);

    ClaimResponse updateClaimStatus(UUID claimId, String newStatus, String updatedBy);

    DocumentResponse uploadDocument(UUID claimId, UUID userId, MultipartFile file);

    List<DocumentResponse> getDocuments(UUID claimId, UUID userId);
}
