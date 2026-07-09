package com.secureclaims.claims.event;

import com.secureclaims.claims.entity.Notification;
import com.secureclaims.claims.repository.NotificationRepository;
import com.secureclaims.events.ClaimCreatedEvent;
import com.secureclaims.events.ClaimStatusUpdatedEvent;
import com.secureclaims.events.FraudAnalysisCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.UUID;

/**
 * Handles domain events and creates notification records.
 * Implements US-013 (Notify on Claim Submitted and Fraud Analysed) and US-014 (Notify on Status Change).
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationRepository notificationRepository;

    /**
     * Notify user when a claim is submitted (US-013).
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Order(10)
    public void handleClaimCreated(final ClaimCreatedEvent event) {
        try {
            final String emailMsg = String.format("Your claim %s has been submitted successfully. We will review it shortly.",
                    event.getClaimId());
            final String smsMsg = String.format("Claim %s submitted. Amount: $%s", event.getClaimId(), event.getClaimedAmount());

            log.info("[EMAIL] To: {} | Subject: Claim Submitted | Body: {}", event.getUserId(), emailMsg);
            log.info("[SMS] To: {} | Message: {}", event.getUserId(), smsMsg);

            saveNotification(event.getUserId(), event.getClaimId(), "CLAIM_SUBMITTED", "EMAIL", emailMsg);
            saveNotification(event.getUserId(), event.getClaimId(), "CLAIM_SUBMITTED", "SMS", smsMsg);

        } catch (final Exception e) {
            log.error("Error sending claim submitted notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Notify user when fraud analysis is completed (US-013).
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Order(10)
    public void handleFraudAnalysisCompleted(final FraudAnalysisCompletedEvent event) {
        try {
            final String emailMsg = String.format("Fraud analysis for claim %s completed. Risk level: %s (score: %d).",
                    event.getClaimId(), event.getRiskLevel(), event.getRiskScore());
            final String smsMsg = String.format("Claim %s analysed. Risk: %s", event.getClaimId(), event.getRiskLevel());

            log.info("[EMAIL] To: {} | Subject: Fraud Analysis Complete | Body: {}", event.getUserId(), emailMsg);
            log.info("[SMS] To: {} | Message: {}", event.getUserId(), smsMsg);

            saveNotification(event.getUserId(), event.getClaimId(), "FRAUD_ANALYSED", "EMAIL", emailMsg);
            saveNotification(event.getUserId(), event.getClaimId(), "FRAUD_ANALYSED", "SMS", smsMsg);

        } catch (final Exception e) {
            log.error("Error sending fraud analysis notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Notify user when claim status changes (US-014).
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Order(10)
    public void handleClaimStatusUpdated(final ClaimStatusUpdatedEvent event) {
        try {
            final String emailMsg = String.format("Your claim %s status changed from %s to %s (updated by: %s).",
                    event.getClaimId(), event.getPreviousStatus(), event.getNewStatus(), event.getUpdatedBy());
            final String smsMsg = String.format("Claim %s: %s → %s", event.getClaimId(), event.getPreviousStatus(), event.getNewStatus());

            log.info("[EMAIL] To: {} | Subject: Claim Status Updated | Body: {}", event.getUserId(), emailMsg);
            log.info("[SMS] To: {} | Message: {}", event.getUserId(), smsMsg);

            saveNotification(event.getUserId(), event.getClaimId(), "STATUS_UPDATED", "EMAIL", emailMsg);
            saveNotification(event.getUserId(), event.getClaimId(), "STATUS_UPDATED", "SMS", smsMsg);

        } catch (final Exception e) {
            log.error("Error sending status update notification: {}", e.getMessage(), e);
        }
    }

    private void saveNotification(final UUID userId, final UUID claimId, final String type,
                                   final String channel, final String message) {
        final Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setClaimId(claimId);
        notification.setType(type);
        notification.setChannel(channel);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }
}
