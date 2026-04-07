package com.taskflow.api.controller;

import com.taskflow.api.dto.response.*;
import com.taskflow.api.dto.response.notification.NotificationResponse;
import com.taskflow.api.service.NotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public PageResponse<NotificationResponse> getNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return notificationService.getNotifications(unreadOnly, page, size);
    }

    @PatchMapping("/{id}/read")
    public ApiResponse markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ApiResponse.of("Notification marked as read.");
    }

    @PostMapping("/read-all")
    public ApiResponse markAllAsRead() {
        notificationService.markAllAsRead();
        return ApiResponse.of("All notifications marked as read.");
    }
}