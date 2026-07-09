package com.secureclaims.claims.event;

import com.secureclaims.claims.entity.FraudAnalysis;
import com.secureclaims.claims.repository.ClaimRepository;
import com.secureclaims.claims.repository.FraudAnalysisRepository;
import com.secureclaims.events.ClaimCreatedEvent;
import com.secureclaims.events.FraudAnalysisCompletedEvent;
import com.secureclaims.events.RiskLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Handles ClaimCreatedEvent by running fraud scoring rules and persisting results.
 * Implements US-011: Fraud Rule Engine & Analysis Persistence.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FraudEventHandler {

    private static final BigDecimal HIGH_AMOUNT = new BigDecimal("50000");
    private static final BigDecimal MEDIUM_AMOUNT = new BigDecimal("10000");
    private static final int YOUNG_POLICY_MONTHS = 6;
    private static final int PRIOR_CLAIMS_THRESHOLD = 3;

    private final FraudAnalysisRepository fraudAnalysisRepository;
    private final ClaimRepository claimRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Listens for ClaimCreatedEvent and runs fraud scoring rules.
     *
     * @param event the claim created event
     */
    @EventListener
    @Transactional
    public void handleClaimCreated(final ClaimCreatedEvent event) {
        try {
            log.info("Fraud analysis started: claimId={}, userId={}", event.getClaimId(), event.getUserId());

            int totalScore = 0;
            final StringBuilder notes = new StringBuilder();

            // Rule 1: Claimed Amount
            final BigDecimal amount = event.getClaimedAmount();
            if (amount.compareTo(HIGH_AMOUNT) > 0) {
                totalScore += 3;
                notes.append(String.format("Amount rule: +3 (claimed $%s > $50,000); ", amount));
            } else if (amount.compareTo(MEDIUM_AMOUNT) >= 0) {
                totalScore += 1;
                notes.append(String.format("Amount rule: +1 (claimed $%s in $10,000-$50,000 range); ", amount));
            } else {
                notes.append(String.format("Amount rule: +0 (claimed $%s < $10,000); ", amount));
            }

            // Rule 2: Policy Age
            if (event.getPolicyAgeMonths() < YOUNG_POLICY_MONTHS) {
                totalScore += 2;
                notes.append(String.format("Policy age rule: +2 (%d months < 6 months); ", event.getPolicyAgeMonths()));
            } else {
                notes.append(String.format("Policy age rule: +0 (%d months >= 6 months); ", event.getPolicyAgeMonths()));
            }

            // Rule 3: Prior Claims in last 12 months
            final long priorClaims = claimRepository.countByUserIdAndSubmittedAtAfter(
                    event.getUserId(), LocalDateTime.now().minusMonths(12));
            if (priorClaims > PRIOR_CLAIMS_THRESHOLD) {
                totalScore += 3;
                notes.append(String.format("Prior claims rule: +3 (%d claims > 3 in last 12 months); ", priorClaims));
            } else {
                notes.append(String.format("Prior claims rule: +0 (%d claims <= 3 in last 12 months); ", priorClaims));
            }

            // Map score to risk level
            final RiskLevel riskLevel;
            if (totalScore >= 4) {
                riskLevel = RiskLevel.HIGH;
            } else if (totalScore >= 2) {
                riskLevel = RiskLevel.MEDIUM;
            } else {
                riskLevel = RiskLevel.LOW;
            }

            notes.append(String.format("Total: %d → %s", totalScore, riskLevel));

            // Save fraud analysis
            final FraudAnalysis analysis = new FraudAnalysis();
            analysis.setClaimId(event.getClaimId());
            analysis.setUserId(event.getUserId());
            analysis.setRiskScore(totalScore);
            analysis.setRiskLevel(riskLevel);
            analysis.setAnalysisNotes(notes.toString());

            final FraudAnalysis saved = fraudAnalysisRepository.save(analysis);
            log.info("Fraud analysis completed: claimId={}, score={}, level={}", event.getClaimId(), totalScore, riskLevel);

            // Publish FraudAnalysisCompletedEvent
            eventPublisher.publishEvent(new FraudAnalysisCompletedEvent(
                    this, event.getClaimId(), event.getUserId(),
                    totalScore, riskLevel, notes.toString(), Instant.now()));

        } catch (final Exception e) {
            log.error("Error during fraud analysis for claimId={}: {}", event.getClaimId(), e.getMessage(), e);
        }
    }
}
