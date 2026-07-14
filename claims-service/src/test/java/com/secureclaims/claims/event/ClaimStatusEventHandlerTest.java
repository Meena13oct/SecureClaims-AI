package com.secureclaims.claims.event;

import com.secureclaims.claims.service.ClaimService;
import com.secureclaims.events.FraudAnalysisCompletedEvent;
import com.secureclaims.events.RiskLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Unit tests for ClaimStatusEventHandler.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class ClaimStatusEventHandlerTest {

    @Mock
    private ClaimService claimService;

    @InjectMocks
    private ClaimStatusEventHandler handler;

    @Test
    void should_rejectClaim_when_riskLevelIsHigh() {
        // given
        final UUID claimId = UUID.randomUUID();
        final FraudAnalysisCompletedEvent event = new FraudAnalysisCompletedEvent(
                this, claimId, UUID.randomUUID(), 8, RiskLevel.HIGH, "High risk", Instant.now());

        // when
        handler.handleFraudAnalysisCompleted(event);

        // then
        verify(claimService).updateClaimStatus(claimId, "REJECTED", "FRAUD_ENGINE");
    }

    @Test
    void should_setUnderReview_when_riskLevelIsMedium() {
        // given
        final UUID claimId = UUID.randomUUID();
        final FraudAnalysisCompletedEvent event = new FraudAnalysisCompletedEvent(
                this, claimId, UUID.randomUUID(), 5, RiskLevel.MEDIUM, "Medium risk", Instant.now());

        // when
        handler.handleFraudAnalysisCompleted(event);

        // then
        verify(claimService).updateClaimStatus(claimId, "UNDER_REVIEW", "FRAUD_ENGINE");
    }

    @Test
    void should_setUnderReview_when_riskLevelIsLow() {
        // given
        final UUID claimId = UUID.randomUUID();
        final FraudAnalysisCompletedEvent event = new FraudAnalysisCompletedEvent(
                this, claimId, UUID.randomUUID(), 1, RiskLevel.LOW, "Low risk", Instant.now());

        // when
        handler.handleFraudAnalysisCompleted(event);

        // then
        verify(claimService).updateClaimStatus(claimId, "UNDER_REVIEW", "FRAUD_ENGINE");
    }

    @Test
    void should_logError_when_serviceThrowsException() {
        // given
        final UUID claimId = UUID.randomUUID();
        final FraudAnalysisCompletedEvent event = new FraudAnalysisCompletedEvent(
                this, claimId, UUID.randomUUID(), 9, RiskLevel.HIGH, "Critical", Instant.now());
        doThrow(new RuntimeException("DB down")).when(claimService).updateClaimStatus(any(), any(), any());

        // when — should not throw
        handler.handleFraudAnalysisCompleted(event);

        // then
        verify(claimService).updateClaimStatus(claimId, "REJECTED", "FRAUD_ENGINE");
    }
}
