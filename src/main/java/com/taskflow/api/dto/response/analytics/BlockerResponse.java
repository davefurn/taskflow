package com.taskflow.api.dto.response.analytics;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data @Builder
public class BlockerResponse {
    private UUID taskId;
    private String taskTitle;
    private UUID blockedByTaskId;
    private String blockedByTaskTitle;
    private String blockedByAssignee;
    private long blockedSinceDays;
}