package com.secureclaims.notification.service;

import com.secureclaims.notification.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface NotificationQueryService {
    Page<NotificationResponse> getNotificationsByUserId(UUID userId, Pageable pageable);
}
