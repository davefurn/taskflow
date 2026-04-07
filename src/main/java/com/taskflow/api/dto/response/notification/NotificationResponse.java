package com.taskflow.api.dto.response.notification;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private String type;
    private String title;
    private String message;
    private String linkUrl;
    private boolean isRead;
    private Instant createdAt;
}