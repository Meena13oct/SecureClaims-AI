package com.secureclaims.notification.controller;

import com.secureclaims.notification.dto.response.ApiResponse;
import com.secureclaims.notification.dto.response.NotificationResponse;
import com.secureclaims.notification.service.NotificationQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications/v1")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Admin endpoints for notification history")
public class NotificationAdminController {

    private final NotificationQueryService notificationQueryService;

    @Operation(summary = "Get notifications for a user", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/admin/notifications/{userId}")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @PathVariable final UUID userId, final Pageable pageable) {
        final Page<NotificationResponse> notifications = notificationQueryService.getNotificationsByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, "Notifications retrieved", notifications));
    }
}
