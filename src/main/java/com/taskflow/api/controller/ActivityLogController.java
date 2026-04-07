package com.taskflow.api.controller;

import com.taskflow.api.dto.response.*;
import com.taskflow.api.dto.response.activityLog.ActivityLogResponse;
import com.taskflow.api.service.ActivityLogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Activity Log")
@SecurityRequirement(name = "bearerAuth")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping("/api/tasks/{taskId}/activity")
    public List<ActivityLogResponse> getTaskActivity(@PathVariable UUID taskId) {
        return activityLogService.getTaskActivity(taskId);
    }

    @GetMapping("/api/projects/{projectId}/activity")
    public PageResponse<ActivityLogResponse> getProjectActivity(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        return activityLogService.getProjectActivity(projectId, page, size);
    }
}