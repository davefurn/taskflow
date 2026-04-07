package com.taskflow.api.controller;


import com.taskflow.api.dto.request.status.CreateStatusRequest;
import com.taskflow.api.dto.request.status.ReorderStatusesRequest;
import com.taskflow.api.dto.request.status.UpdateStatusRequest;
import com.taskflow.api.dto.response.*;
import com.taskflow.api.dto.response.status.TaskStatusResponse;
import com.taskflow.api.service.TaskStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/statuses")
@RequiredArgsConstructor
@Tag(name = "Task Statuses")
@SecurityRequirement(name = "bearerAuth")
public class TaskStatusController {

    private final TaskStatusService taskStatusService;

    @GetMapping
    @Operation(summary = "Get all statuses for a project")
    public List<TaskStatusResponse> getStatuses(@PathVariable UUID projectId) {
        return taskStatusService.getStatuses(projectId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new status — project lead or admin")
    public TaskStatusResponse createStatus(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateStatusRequest request) {
        return taskStatusService.createStatus(projectId, request);
    }

    @PutMapping("/{statusId}")
    @Operation(summary = "Update a status")
    public TaskStatusResponse updateStatus(
            @PathVariable UUID projectId,
            @PathVariable UUID statusId,
            @Valid @RequestBody UpdateStatusRequest request) {
        return taskStatusService.updateStatus(projectId, statusId, request);
    }

    @PutMapping("/reorder")
    @Operation(summary = "Reorder statuses by dragging")
    public ApiResponse reorderStatuses(
            @PathVariable UUID projectId,
            @Valid @RequestBody ReorderStatusesRequest request) {
        taskStatusService.reorderStatuses(projectId, request);
        return ApiResponse.of("Statuses reordered.");
    }

    @DeleteMapping("/{statusId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a status — reassign tasks first if needed")
    public void deleteStatus(
            @PathVariable UUID projectId,
            @PathVariable UUID statusId,
            @RequestParam(required = false) UUID reassignTo) {
        taskStatusService.deleteStatus(projectId, statusId, reassignTo);
    }
}