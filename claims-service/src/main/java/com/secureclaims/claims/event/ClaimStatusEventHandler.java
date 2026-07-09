package com.secureclaims.claims.event;

import com.secureclaims.claims.service.ClaimService;
import com.secureclaims.events.FraudAnalysisCompletedEvent;
import com.secureclaims.events.RiskLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

/**
 * Listens for FraudAnalysisCompletedEvent and automatically updates claim status.
 * Implements US-012: Automatic Claim Status Update from Fraud Result.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimStatusEventHandler {

    private static final String FRAUD_ENGINE = "FRAUD_ENGINE";

    private final ClaimService claimService;

    /**
     * Updates claim status based on fraud analysis result.
     * HIGH risk → REJECTED, LOW/MEDIUM → UNDER_REVIEW.
     *
     * @param event the fraud analysis completed event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFraudAnalysisCompleted(final FraudAnalysisCompletedEvent event) {
        try {
            final String newStatus;
            if (event.getRiskLevel() == RiskLevel.HIGH) {
                newStatus = "REJECTED";
            } else {
                newStatus = "UNDER_REVIEW";
            }

            log.info("Auto-updating claim status: claimId={}, riskLevel={}, newStatus={}",
                    event.getClaimId(), event.getRiskLevel(), newStatus);

            claimService.updateClaimStatus(event.getClaimId(), newStatus, FRAUD_ENGINE);

        } catch (final Exception e) {
            log.error("Error updating claim status from fraud result: claimId={}, error={}",
                    event.getClaimId(), e.getMessage(), e);
        }
    }
}
