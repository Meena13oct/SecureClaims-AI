package com.secureclaims.notification.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID notificationId;
    private UUID userId;
    private UUID claimId;
    private String type;
    private String channel;
    private String message;
    private LocalDateTime sentAt;
}
