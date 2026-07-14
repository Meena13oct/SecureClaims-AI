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
import com.secureclaims.events.ClaimCreatedEvent;
import com.secureclaims.events.ClaimStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClaimServiceImpl.
 * Covers US-007 (create claim), US-008 (view claims), US-009 (status transitions).
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class ClaimServiceImplTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ClaimServiceImpl claimService;

    @Test
    void should_createClaim_when_validRequest() {
        // given
        final UUID userId = UUID.randomUUID();
        final ClaimRequest request = new ClaimRequest();
        request.setPolicyNumber("POL-2026-001");
        request.setClaimType("MEDICAL");
        request.setIncidentDate(LocalDate.of(2026, 6, 20));
        request.setDescription("Surgery");
        request.setClaimedAmount(new BigDecimal("75000"));
        request.setPolicyAgeMonths(12);

        final Claim savedClaim = buildClaim(userId, ClaimStatus.SUBMITTED);
        when(claimRepository.save(any(Claim.class))).thenReturn(savedClaim);

        // when
        final ClaimResponse response = claimService.createClaim(request, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUBMITTED");
        assertThat(response.getUserId()).isEqualTo(userId);
        verify(claimRepository).save(any(Claim.class));
        verify(eventPublisher).publishEvent(any(ClaimCreatedEvent.class));
    }

    @Test
    void should_publishClaimCreatedEvent_when_claimCreated() {
        // given
        final UUID userId = UUID.randomUUID();
        final ClaimRequest request = new ClaimRequest();
        request.setPolicyNumber("POL-2026-002");
        request.setClaimType("AUTO");
        request.setIncidentDate(LocalDate.of(2026, 6, 15));
        request.setDescription("Car accident");
        request.setClaimedAmount(new BigDecimal("25000"));
        request.setPolicyAgeMonths(3);

        final Claim savedClaim = buildClaim(userId, ClaimStatus.SUBMITTED);
        when(claimRepository.save(any(Claim.class))).thenReturn(savedClaim);

        // when
        claimService.createClaim(request, userId);

        // then
        final ArgumentCaptor<ClaimCreatedEvent> captor = ArgumentCaptor.forClass(ClaimCreatedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        final ClaimCreatedEvent event = captor.getValue();
        assertThat(event.getClaimId()).isEqualTo(savedClaim.getId());
        assertThat(event.getUserId()).isEqualTo(userId);
    }

    @Test
    void should_returnClaim_when_userOwnsIt() {
        // given
        final UUID userId = UUID.randomUUID();
        final Claim claim = buildClaim(userId, ClaimStatus.SUBMITTED);
        when(claimRepository.findById(claim.getId())).thenReturn(Optional.of(claim));

        // when
        final ClaimResponse response = claimService.getClaimById(claim.getId(), userId);

        // then
        assertThat(response.getClaimId()).isEqualTo(claim.getId());
    }

    @Test
    void should_throwAccessDenied_when_userDoesNotOwnClaim() {
        // given
        final UUID ownerId = UUID.randomUUID();
        final UUID otherUserId = UUID.randomUUID();
        final Claim claim = buildClaim(ownerId, ClaimStatus.SUBMITTED);
        when(claimRepository.findById(claim.getId())).thenReturn(Optional.of(claim));

        // when/then
        assertThatThrownBy(() -> claimService.getClaimById(claim.getId(), otherUserId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void should_throwNotFound_when_claimDoesNotExist() {
        // given
        final UUID claimId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        when(claimRepository.findById(claimId)).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> claimService.getClaimById(claimId, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void should_updateStatus_when_validTransition() {
        // given
        final Claim claim = buildClaim(UUID.randomUUID(), ClaimStatus.SUBMITTED);
        when(claimRepository.findById(claim.getId())).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        // when
        final ClaimResponse response = claimService.updateClaimStatus(claim.getId(), "UNDER_REVIEW", "ADMIN");

        // then
        verify(claimRepository).save(any(Claim.class));
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void should_throwInvalidTransition_when_invalidStatusChange() {
        // given
        final Claim claim = buildClaim(UUID.randomUUID(), ClaimStatus.SUBMITTED);
        when(claimRepository.findById(claim.getId())).thenReturn(Optional.of(claim));

        // when/then
        assertThatThrownBy(() -> claimService.updateClaimStatus(claim.getId(), "CLOSED", "ADMIN"))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("Cannot transition from SUBMITTED to CLOSED");
    }

    @Test
    void should_throwInvalidTransition_when_invalidStatusValue() {
        // given
        final Claim claim = buildClaim(UUID.randomUUID(), ClaimStatus.SUBMITTED);
        when(claimRepository.findById(claim.getId())).thenReturn(Optional.of(claim));

        // when/then
        assertThatThrownBy(() -> claimService.updateClaimStatus(claim.getId(), "INVALID_STATUS", "ADMIN"))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("Invalid status");
    }

    @Test
    void should_returnPageOfClaims_when_getAllClaims() {
        // given
        final Claim claim = buildClaim(UUID.randomUUID(), ClaimStatus.SUBMITTED);
        final Page<Claim> page = new PageImpl<>(List.of(claim));
        when(claimRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // when
        final Page<ClaimResponse> result = claimService.getAllClaims(PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getClaimId()).isEqualTo(claim.getId());
    }

    @Test
    void should_returnUserClaims_when_getClaimsByUser() {
        // given
        final UUID userId = UUID.randomUUID();
        final Claim claim = buildClaim(userId, ClaimStatus.SUBMITTED);
        final Page<Claim> page = new PageImpl<>(List.of(claim));
        when(claimRepository.findByUserId(eq(userId), any())).thenReturn(page);

        // when
        final Page<ClaimResponse> result = claimService.getClaimsByUser(userId, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(userId);
    }

    @Test
    void should_returnDocuments_when_userOwnsClaimForDocuments() {
        // given
        final UUID userId = UUID.randomUUID();
        final Claim claim = buildClaim(userId, ClaimStatus.SUBMITTED);
        final Document doc = new Document();
        doc.setId(UUID.randomUUID());
        doc.setClaimId(claim.getId());
        doc.setOriginalFilename("policy.pdf");
        doc.setStoredFilename("stored.pdf");
        doc.setFilePath("/path/to/stored.pdf");
        doc.setMimeType("application/pdf");
        doc.setFileSizeBytes(1024L);
        doc.setUploadedAt(LocalDateTime.now());

        when(claimRepository.findById(claim.getId())).thenReturn(Optional.of(claim));
        when(documentRepository.findByClaimId(claim.getId())).thenReturn(List.of(doc));

        // when
        final var documents = claimService.getDocuments(claim.getId(), userId);

        // then
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getOriginalFilename()).isEqualTo("policy.pdf");
    }

    @Test
    void should_throwAccessDenied_when_userDoesNotOwnClaimForDocuments() {
        // given
        final UUID ownerId = UUID.randomUUID();
        final UUID otherUser = UUID.randomUUID();
        final Claim claim = buildClaim(ownerId, ClaimStatus.SUBMITTED);
        when(claimRepository.findById(claim.getId())).thenReturn(Optional.of(claim));

        // when/then
        assertThatThrownBy(() -> claimService.getDocuments(claim.getId(), otherUser))
                .isInstanceOf(AccessDeniedException.class);
    }

    private Claim buildClaim(final UUID userId, final ClaimStatus status) {
        final Claim claim = new Claim();
        claim.setId(UUID.randomUUID());
        claim.setUserId(userId);
        claim.setPolicyNumber("POL-2026-001");
        claim.setClaimType("MEDICAL");
        claim.setIncidentDate(LocalDate.of(2026, 6, 20));
        claim.setDescription("Test claim");
        claim.setClaimedAmount(new BigDecimal("75000"));
        claim.setStatus(status);
        claim.setPolicyAgeMonths(12);
        claim.setSubmittedAt(LocalDateTime.now());
        claim.setUpdatedAt(LocalDateTime.now());
        return claim;
    }
}
