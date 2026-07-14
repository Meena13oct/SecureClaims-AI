package com.secureclaims.notification.service.impl;

import com.secureclaims.notification.dto.response.NotificationResponse;
import com.secureclaims.notification.entity.Notification;
import com.secureclaims.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for NotificationQueryServiceImpl.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceImplTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private NotificationQueryServiceImpl service;

    @Test
    void should_returnNotifications_when_userHasNotifications() {
        // given
        final UUID userId = UUID.randomUUID();
        final Pageable pageable = PageRequest.of(0, 10);
        final Notification notification = createNotification(userId);
        final Page<Notification> page = new PageImpl<>(List.of(notification), pageable, 1);
        when(repository.findByUserId(userId, pageable)).thenReturn(page);

        // when
        final Page<NotificationResponse> result = service.getNotificationsByUserId(userId, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(userId);
        assertThat(result.getContent().get(0).getType()).isEqualTo("CLAIM_STATUS_CHANGE");
        assertThat(result.getContent().get(0).getChannel()).isEqualTo("EMAIL");
    }

    @Test
    void should_returnEmptyPage_when_noNotificationsExist() {
        // given
        final UUID userId = UUID.randomUUID();
        final Pageable pageable = PageRequest.of(0, 10);
        final Page<Notification> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(repository.findByUserId(userId, pageable)).thenReturn(emptyPage);

        // when
        final Page<NotificationResponse> result = service.getNotificationsByUserId(userId, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void should_mapAllFieldsCorrectly_when_notificationExists() {
        // given
        final UUID userId = UUID.randomUUID();
        final UUID claimId = UUID.randomUUID();
        final UUID notificationId = UUID.randomUUID();
        final LocalDateTime sentAt = LocalDateTime.now();
        final Pageable pageable = PageRequest.of(0, 10);

        final Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setUserId(userId);
        notification.setClaimId(claimId);
        notification.setType("FRAUD_ALERT");
        notification.setChannel("SMS");
        notification.setMessage("Your claim is flagged");
        notification.setSentAt(sentAt);

        final Page<Notification> page = new PageImpl<>(List.of(notification), pageable, 1);
        when(repository.findByUserId(userId, pageable)).thenReturn(page);

        // when
        final Page<NotificationResponse> result = service.getNotificationsByUserId(userId, pageable);

        // then
        final NotificationResponse response = result.getContent().get(0);
        assertThat(response.getNotificationId()).isEqualTo(notificationId);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getClaimId()).isEqualTo(claimId);
        assertThat(response.getType()).isEqualTo("FRAUD_ALERT");
        assertThat(response.getChannel()).isEqualTo("SMS");
        assertThat(response.getMessage()).isEqualTo("Your claim is flagged");
        assertThat(response.getSentAt()).isEqualTo(sentAt);
    }

    private Notification createNotification(final UUID userId) {
        final Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setUserId(userId);
        notification.setClaimId(UUID.randomUUID());
        notification.setType("CLAIM_STATUS_CHANGE");
        notification.setChannel("EMAIL");
        notification.setMessage("Your claim status has changed");
        notification.setSentAt(LocalDateTime.now());
        return notification;
    }
}
