package com.secureclaims.claims.event;

import com.secureclaims.claims.entity.Claim;
import com.secureclaims.claims.entity.FraudAnalysis;
import com.secureclaims.claims.repository.ClaimRepository;
import com.secureclaims.claims.repository.FraudAnalysisRepository;
import com.secureclaims.events.ClaimCreatedEvent;
import com.secureclaims.events.ClaimStatus;
import com.secureclaims.events.FraudAnalysisCompletedEvent;
import com.secureclaims.events.RiskLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FraudEventHandler.
 * Tests US-011 fraud scoring rules at boundary values.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class FraudEventHandlerTest {

    @Mock
    private FraudAnalysisRepository fraudAnalysisRepository;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FraudEventHandler fraudEventHandler;

    @Test
    void should_scoreHigh_when_amountExceeds50000() {
        // given - amount > 50000 (+3), policy age >= 6 months (+0), prior claims <= 3 (+0) = 3 → MEDIUM
        // Actually need total >= 4 for HIGH. Let's test amount > 50000 with young policy.
        final UUID claimId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final ClaimCreatedEvent event = new ClaimCreatedEvent(
                this, claimId, userId, "POL-001", "MEDICAL",
                new BigDecimal("60000"), 3, Instant.now());

        final Claim claim = buildClaim(claimId, userId);
        when(claimRepository.countByUserIdAndSubmittedAtAfter(eq(userId), any(LocalDateTime.class))).thenReturn(0L);
        when(fraudAnalysisRepository.save(any(FraudAnalysis.class))).thenAnswer(i -> i.getArgument(0));
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));

        // when
        fraudEventHandler.handleClaimCreated(event);

        // then - amount +3, policy age < 6 months +2 = 5 → HIGH
        final ArgumentCaptor<FraudAnalysis> captor = ArgumentCaptor.forClass(FraudAnalysis.class);
        verify(fraudAnalysisRepository).save(captor.capture());
        assertThat(captor.getValue().getRiskScore()).isEqualTo(5);
        assertThat(captor.getValue().getRiskLevel()).isEqualTo(RiskLevel.HIGH);
    }

    @Test
    void should_scoreMedium_when_amountBetween10000And50000() {
        // given - amount $25000 (+1), policy age 3 months (+2), prior claims 0 (+0) = 3 → MEDIUM
        final UUID claimId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final ClaimCreatedEvent event = new ClaimCreatedEvent(
                this, claimId, userId, "POL-002", "AUTO",
                new BigDecimal("25000"), 3, Instant.now());

        final Claim claim = buildClaim(claimId, userId);
        when(claimRepository.countByUserIdAndSubmittedAtAfter(eq(userId), any(LocalDateTime.class))).thenReturn(0L);
        when(fraudAnalysisRepository.save(any(FraudAnalysis.class))).thenAnswer(i -> i.getArgument(0));
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));

        // when
        fraudEventHandler.handleClaimCreated(event);

        // then
        final ArgumentCaptor<FraudAnalysis> captor = ArgumentCaptor.forClass(FraudAnalysis.class);
        verify(fraudAnalysisRepository).save(captor.capture());
        assertThat(captor.getValue().getRiskScore()).isEqualTo(3);
        assertThat(captor.getValue().getRiskLevel()).isEqualTo(RiskLevel.MEDIUM);
    }

    @Test
    void should_scoreLow_when_allRulesBelowThreshold() {
        // given - amount $5000 (+0), policy age 12 months (+0), prior claims 1 (+0) = 0 → LOW
        final UUID claimId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final ClaimCreatedEvent event = new ClaimCreatedEvent(
                this, claimId, userId, "POL-003", "PROPERTY",
                new BigDecimal("5000"), 12, Instant.now());

        final Claim claim = buildClaim(claimId, userId);
        when(claimRepository.countByUserIdAndSubmittedAtAfter(eq(userId), any(LocalDateTime.class))).thenReturn(1L);
        when(fraudAnalysisRepository.save(any(FraudAnalysis.class))).thenAnswer(i -> i.getArgument(0));
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));

        // when
        fraudEventHandler.handleClaimCreated(event);

        // then
        final ArgumentCaptor<FraudAnalysis> captor = ArgumentCaptor.forClass(FraudAnalysis.class);
        verify(fraudAnalysisRepository).save(captor.capture());
        assertThat(captor.getValue().getRiskScore()).isEqualTo(0);
        assertThat(captor.getValue().getRiskLevel()).isEqualTo(RiskLevel.LOW);
    }

    @Test
    void should_addPriorClaimsPoints_when_moreThan3Claims() {
        // given - amount $5000 (+0), policy age 12 months (+0), prior claims 5 (+3) = 3 → MEDIUM
        final UUID claimId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final ClaimCreatedEvent event = new ClaimCreatedEvent(
                this, claimId, userId, "POL-004", "TRAVEL",
                new BigDecimal("5000"), 12, Instant.now());

        final Claim claim = buildClaim(claimId, userId);
        when(claimRepository.countByUserIdAndSubmittedAtAfter(eq(userId), any(LocalDateTime.class))).thenReturn(5L);
        when(fraudAnalysisRepository.save(any(FraudAnalysis.class))).thenAnswer(i -> i.getArgument(0));
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));

        // when
        fraudEventHandler.handleClaimCreated(event);

        // then
        final ArgumentCaptor<FraudAnalysis> captor = ArgumentCaptor.forClass(FraudAnalysis.class);
        verify(fraudAnalysisRepository).save(captor.capture());
        assertThat(captor.getValue().getRiskScore()).isEqualTo(3);
        assertThat(captor.getValue().getRiskLevel()).isEqualTo(RiskLevel.MEDIUM);
    }

    @Test
    void should_rejectClaim_when_riskIsHigh() {
        // given - amount $60000 (+3), policy age 2 months (+2), prior claims 5 (+3) = 8 → HIGH
        final UUID claimId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final ClaimCreatedEvent event = new ClaimCreatedEvent(
                this, claimId, userId, "POL-005", "MEDICAL",
                new BigDecimal("60000"), 2, Instant.now());

        final Claim claim = buildClaim(claimId, userId);
        when(claimRepository.countByUserIdAndSubmittedAtAfter(eq(userId), any(LocalDateTime.class))).thenReturn(5L);
        when(fraudAnalysisRepository.save(any(FraudAnalysis.class))).thenAnswer(i -> i.getArgument(0));
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));

        // when
        fraudEventHandler.handleClaimCreated(event);

        // then - claim should be set to REJECTED
        verify(claimRepository).save(argThat(c -> c.getStatus() == ClaimStatus.REJECTED));
    }

    @Test
    void should_publishFraudAnalysisCompletedEvent() {
        // given
        final UUID claimId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final ClaimCreatedEvent event = new ClaimCreatedEvent(
                this, claimId, userId, "POL-006", "AUTO",
                new BigDecimal("5000"), 12, Instant.now());

        final Claim claim = buildClaim(claimId, userId);
        when(claimRepository.countByUserIdAndSubmittedAtAfter(eq(userId), any(LocalDateTime.class))).thenReturn(0L);
        when(fraudAnalysisRepository.save(any(FraudAnalysis.class))).thenAnswer(i -> i.getArgument(0));
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));

        // when
        fraudEventHandler.handleClaimCreated(event);

        // then
        verify(eventPublisher).publishEvent(any(FraudAnalysisCompletedEvent.class));
    }

    private Claim buildClaim(final UUID claimId, final UUID userId) {
        final Claim claim = new Claim();
        claim.setId(claimId);
        claim.setUserId(userId);
        claim.setPolicyNumber("POL-001");
        claim.setClaimType("MEDICAL");
        claim.setIncidentDate(LocalDate.of(2026, 6, 20));
        claim.setDescription("Test");
        claim.setClaimedAmount(new BigDecimal("50000"));
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setPolicyAgeMonths(12);
        claim.setSubmittedAt(LocalDateTime.now());
        claim.setUpdatedAt(LocalDateTime.now());
        return claim;
    }
}
