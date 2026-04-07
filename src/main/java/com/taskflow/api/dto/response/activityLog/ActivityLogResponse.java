package com.taskflow.api.dto.response.activityLog;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ActivityLogResponse {
    private UUID id;
    private UUID userId;
    private String userName;
    private String action;
    private String fieldChanged;
    private String oldValue;
    private String newValue;
    private Instant createdAt;
}