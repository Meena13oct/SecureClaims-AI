package com.secureclaims.claims.service.impl;

import com.secureclaims.claims.dto.request.ClaimRequest;
import com.secureclaims.claims.dto.response.ClaimResponse;
import com.secureclaims.claims.dto.response.DocumentResponse;
import com.secureclaims.claims.entity.Claim;
import com.secureclaims.claims.entity.Document;
import com.secureclaims.claims.exception.InvalidStatusTransitionException;
import com.secureclaims.claims.exception.ResourceNotFoundException;
import com.secureclaims.claims.repository.ClaimRepository;
import com.secureclaims.claims.repository.DocumentRepository;
import com.secureclaims.claims.service.ClaimService;
import com.secureclaims.events.ClaimCreatedEvent;
import com.secureclaims.events.ClaimStatus;
import com.secureclaims.events.ClaimStatusUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of claim operations including submission, viewing, status management, and document upload.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {

    private static final Map<ClaimStatus, Set<ClaimStatus>> VALID_TRANSITIONS = Map.of(
            ClaimStatus.SUBMITTED, Set.of(ClaimStatus.UNDER_REVIEW, ClaimStatus.REJECTED),
            ClaimStatus.UNDER_REVIEW, Set.of(ClaimStatus.APPROVED, ClaimStatus.REJECTED),
            ClaimStatus.APPROVED, Set.of(ClaimStatus.CLOSED),
            ClaimStatus.REJECTED, Set.of(ClaimStatus.CLOSED)
    );

    private final ClaimRepository claimRepository;
    private final DocumentRepository documentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.upload.base-path:uploads/claims}")
    private String uploadBasePath;

    @Override
    @Transactional
    public ClaimResponse createClaim(final ClaimRequest request, final UUID userId) {
        final Claim claim = new Claim();
        claim.setUserId(userId);
        claim.setPolicyNumber(request.getPolicyNumber());
        claim.setClaimType(request.getClaimType());
        claim.setIncidentDate(request.getIncidentDate());
        claim.setDescription(request.getDescription());
        claim.setClaimedAmount(request.getClaimedAmount());
        claim.setPolicyAgeMonths(request.getPolicyAgeMonths());
        claim.setStatus(ClaimStatus.SUBMITTED);

        final Claim saved = claimRepository.save(claim);
        log.info("Claim created: claimId={}, userId={}", saved.getId(), userId);

        // Publish ClaimCreatedEvent for fraud analysis
        eventPublisher.publishEvent(new ClaimCreatedEvent(
                this, saved.getId(), userId, saved.getPolicyNumber(),
                saved.getClaimType(), saved.getClaimedAmount(),
                saved.getPolicyAgeMonths(), Instant.now()));

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClaimResponse> getClaimsByUser(final UUID userId, final Pageable pageable) {
        return claimRepository.findByUserId(userId, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimResponse getClaimById(final UUID claimId, final UUID userId) {
        final Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", claimId));

        if (!claim.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not own this claim");
        }
        return mapToResponse(claim);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClaimResponse> getAllClaims(final Pageable pageable) {
        return claimRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public ClaimResponse updateClaimStatus(final UUID claimId, final String newStatus, final String updatedBy) {
        final Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", claimId));

        final ClaimStatus targetStatus;
        try {
            targetStatus = ClaimStatus.valueOf(newStatus);
        } catch (final IllegalArgumentException e) {
            throw new InvalidStatusTransitionException("Invalid status: " + newStatus);
        }

        // Validate transition
        final Set<ClaimStatus> allowed = VALID_TRANSITIONS.getOrDefault(claim.getStatus(), Set.of());
        if (!allowed.contains(targetStatus)) {
            throw new InvalidStatusTransitionException(
                    String.format("Cannot transition from %s to %s", claim.getStatus(), targetStatus));
        }

        final ClaimStatus previousStatus = claim.getStatus();
        claim.setStatus(targetStatus);
        claim.setUpdatedBy(updatedBy);
        final Claim saved = claimRepository.save(claim);
        log.info("Claim status updated: claimId={}, {} -> {}, by={}", claimId, previousStatus, targetStatus, updatedBy);

        // Publish status updated event
        eventPublisher.publishEvent(new ClaimStatusUpdatedEvent(
                this, claimId, claim.getUserId(), previousStatus, targetStatus, updatedBy, Instant.now()));

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DocumentResponse uploadDocument(final UUID claimId, final UUID userId, final MultipartFile file) {
        final Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", claimId));

        if (!claim.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not own this claim");
        }

        // Validate PDF only
        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Only PDF files are accepted");
        }

        // Save file to filesystem
        final String storedFilename = UUID.randomUUID() + ".pdf";
        final Path dirPath = Paths.get(uploadBasePath, claimId.toString());
        final Path filePath = dirPath.resolve(storedFilename);

        try {
            Files.createDirectories(dirPath);
            Files.write(filePath, file.getBytes());
        } catch (final IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }

        // Save metadata
        final Document doc = new Document();
        doc.setClaimId(claimId);
        doc.setOriginalFilename(file.getOriginalFilename());
        doc.setStoredFilename(storedFilename);
        doc.setFilePath(filePath.toString());
        doc.setMimeType(file.getContentType());
        doc.setFileSizeBytes(file.getSize());

        final Document saved = documentRepository.save(doc);
        log.info("Document uploaded: docId={}, claimId={}", saved.getId(), claimId);

        return mapToDocResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocuments(final UUID claimId, final UUID userId) {
        final Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", claimId));

        if (!claim.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not own this claim");
        }

        return documentRepository.findByClaimId(claimId).stream()
                .map(this::mapToDocResponse)
                .toList();
    }

    private ClaimResponse mapToResponse(final Claim claim) {
        return ClaimResponse.builder()
                .claimId(claim.getId())
                .userId(claim.getUserId())
                .policyNumber(claim.getPolicyNumber())
                .claimType(claim.getClaimType())
                .incidentDate(claim.getIncidentDate())
                .description(claim.getDescription())
                .claimedAmount(claim.getClaimedAmount())
                .status(claim.getStatus().name())
                .submittedAt(claim.getSubmittedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }

    private DocumentResponse mapToDocResponse(final Document doc) {
        return DocumentResponse.builder()
                .documentId(doc.getId())
                .originalFilename(doc.getOriginalFilename())
                .filePath(doc.getFilePath())
                .mimeType(doc.getMimeType())
                .fileSizeBytes(doc.getFileSizeBytes())
                .uploadedAt(doc.getUploadedAt())
                .build();
    }
}
