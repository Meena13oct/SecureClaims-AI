package com.secureclaims.claims.event;

import com.secureclaims.claims.entity.Notification;
import com.secureclaims.claims.repository.NotificationRepository;
import com.secureclaims.events.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationEventHandler.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class NotificationEventHandlerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationEventHandler handler;

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    @Test
    void should_saveTwoNotifications_when_claimCreated() {
        // given
        final ClaimCreatedEvent event = new ClaimCreatedEvent(
                this, UUID.randomUUID(), UUID.randomUUID(), "POL-123",
                "AUTO", BigDecimal.valueOf(5000), 12, Instant.now());

        // when
        handler.handleClaimCreated(event);

        // then — EMAIL + SMS
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());
        assertThat(notificationCaptor.getAllValues()).hasSize(2);
        assertThat(notificationCaptor.getAllValues().get(0).getType()).isEqualTo("CLAIM_SUBMITTED");
        assertThat(notificationCaptor.getAllValues().get(0).getChannel()).isEqualTo("EMAIL");
        assertThat(notificationCaptor.getAllValues().get(1).getChannel()).isEqualTo("SMS");
    }

    @Test
    void should_saveTwoNotifications_when_fraudAnalysisCompleted() {
        // given
        final FraudAnalysisCompletedEvent event = new FraudAnalysisCompletedEvent(
                this, UUID.randomUUID(), UUID.randomUUID(), 5, RiskLevel.MEDIUM,
                "Medium risk flags", Instant.now());

        // when
        handler.handleFraudAnalysisCompleted(event);

        // then
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());
        assertThat(notificationCaptor.getAllValues().get(0).getType()).isEqualTo("FRAUD_ANALYSED");
        assertThat(notificationCaptor.getAllValues().get(1).getType()).isEqualTo("FRAUD_ANALYSED");
    }

    @Test
    void should_saveTwoNotifications_when_claimStatusUpdated() {
        // given
        final ClaimStatusUpdatedEvent event = new ClaimStatusUpdatedEvent(
                this, UUID.randomUUID(), UUID.randomUUID(),
                ClaimStatus.SUBMITTED, ClaimStatus.UNDER_REVIEW,
                "admin", Instant.now());

        // when
        handler.handleClaimStatusUpdated(event);

        // then
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());
        assertThat(notificationCaptor.getAllValues().get(0).getType()).isEqualTo("STATUS_UPDATED");
        assertThat(notificationCaptor.getAllValues().get(0).getChannel()).isEqualTo("EMAIL");
        assertThat(notificationCaptor.getAllValues().get(1).getChannel()).isEqualTo("SMS");
    }

    @Test
    void should_notThrow_when_claimCreatedHandlerFails() {
        // given
        doThrow(new RuntimeException("DB error")).when(notificationRepository).save(any());
        final ClaimCreatedEvent event = new ClaimCreatedEvent(
                this, UUID.randomUUID(), UUID.randomUUID(), "POL-456",
                "HEALTH", BigDecimal.valueOf(2000), 6, Instant.now());

        // when — should not throw
        handler.handleClaimCreated(event);

        // then — save was attempted
        verify(notificationRepository, atLeastOnce()).save(any());
    }

    @Test
    void should_notThrow_when_fraudAnalysisHandlerFails() {
        // given
        doThrow(new RuntimeException("DB error")).when(notificationRepository).save(any());
        final FraudAnalysisCompletedEvent event = new FraudAnalysisCompletedEvent(
                this, UUID.randomUUID(), UUID.randomUUID(), 9, RiskLevel.HIGH,
                "Very suspicious", Instant.now());

        // when
        handler.handleFraudAnalysisCompleted(event);

        // then
        verify(notificationRepository, atLeastOnce()).save(any());
    }

    @Test
    void should_notThrow_when_statusUpdateHandlerFails() {
        // given
        doThrow(new RuntimeException("DB error")).when(notificationRepository).save(any());
        final ClaimStatusUpdatedEvent event = new ClaimStatusUpdatedEvent(
                this, UUID.randomUUID(), UUID.randomUUID(),
                ClaimStatus.UNDER_REVIEW, ClaimStatus.APPROVED,
                "manager", Instant.now());

        // when
        handler.handleClaimStatusUpdated(event);

        // then
        verify(notificationRepository, atLeastOnce()).save(any());
    }

    @Test
    void should_setCorrectFields_when_notificationSaved() {
        // given
        final UUID userId = UUID.randomUUID();
        final UUID claimId = UUID.randomUUID();
        final ClaimCreatedEvent event = new ClaimCreatedEvent(
                this, claimId, userId, "POL-789",
                "PROPERTY", BigDecimal.valueOf(10000), 24, Instant.now());

        // when
        handler.handleClaimCreated(event);

        // then
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());
        final Notification emailNotification = notificationCaptor.getAllValues().get(0);
        assertThat(emailNotification.getUserId()).isEqualTo(userId);
        assertThat(emailNotification.getClaimId()).isEqualTo(claimId);
        assertThat(emailNotification.getMessage()).contains(claimId.toString());
    }
}
