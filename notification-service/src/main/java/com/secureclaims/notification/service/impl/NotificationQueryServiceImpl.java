package com.secureclaims.notification.service.impl;

import com.secureclaims.notification.dto.response.NotificationResponse;
import com.secureclaims.notification.repository.NotificationRepository;
import com.secureclaims.notification.service.NotificationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationQueryServiceImpl implements NotificationQueryService {

    private final NotificationRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsByUserId(final UUID userId, final Pageable pageable) {
        return repository.findByUserId(userId, pageable).map(n -> NotificationResponse.builder()
                .notificationId(n.getId())
                .userId(n.getUserId())
                .claimId(n.getClaimId())
                .type(n.getType())
                .channel(n.getChannel())
                .message(n.getMessage())
                .sentAt(n.getSentAt())
                .build());
    }
}
