package com.taskflow.api.controller;


import com.taskflow.api.dto.request.task.*;
import com.taskflow.api.dto.response.*;
import com.taskflow.api.dto.response.task.TaskDetailResponse;
import com.taskflow.api.dto.response.task.TaskSummaryResponse;
import com.taskflow.api.entity.Task;
import com.taskflow.api.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Tasks")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/api/projects/{projectId}/tasks")
    @Operation(summary = "List tasks - filterable, sortable, paginated")
    public PageResponse<TaskSummaryResponse> getTasks(
            @PathVariable UUID projectId,
            @RequestParam(required = false) UUID statusId,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) Task.Priority priority,
            @RequestParam(required = false) UUID labelId,
            @RequestParam(required = false) UUID groupId,
            @RequestParam(required = false) LocalDate dueBefore,
            @RequestParam(required = false) LocalDate dueAfter,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        return taskService.getTasks(projectId, statusId, assigneeId, priority,
                labelId, groupId, dueBefore, dueAfter, search, sort, order, page, size);
    }

    @GetMapping("/api/tasks/{id}")
    @Operation(summary = "Get full task detail")
    public TaskDetailResponse getTask(@PathVariable UUID id) {
        return taskService.getTask(id);
    }

    @PostMapping("/api/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a task")
    public TaskDetailResponse createTask(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskRequest request) {
        return taskService.createTask(projectId, request);
    }

    @PutMapping("/api/tasks/{id}")
    @Operation(summary = "Update a task - logs activity, sends notifications")
    public TaskDetailResponse updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.updateTask(id, request);
    }

    @PatchMapping("/api/tasks/{id}/status")
    @Operation(summary = "Quick status change - used by board drag-and-drop")
    public TaskDetailResponse patchStatus(
            @PathVariable UUID id,
            @Valid @RequestBody PatchTaskStatusRequest request) {
        return taskService.patchStatus(id, request);
    }

    @PatchMapping("/api/tasks/{id}/assignees")
    @Operation(summary = "Update task assignees")
    public TaskDetailResponse patchAssignees(
            @PathVariable UUID id,
            @Valid @RequestBody PatchTaskAssigneesRequest request) {
        return taskService.patchAssignees(id, request);
    }

    @PatchMapping("/api/tasks/{id}/position")
    @Operation(summary = "Update task position - used by list/board drag-and-drop")
    public ApiResponse patchPosition(
            @PathVariable UUID id,
            @Valid @RequestBody PatchTaskPositionRequest request) {
        taskService.patchPosition(id, request);
        return ApiResponse.of("Position updated.");
    }

    @DeleteMapping("/api/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a task")
    public void deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
    }

    @PostMapping("/api/tasks/{id}/duplicate")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Duplicate a task with title 'Copy of ...'")
    public TaskDetailResponse duplicateTask(@PathVariable UUID id) {
        return taskService.duplicateTask(id);
    }

    @GetMapping("/api/my-tasks")
    @Operation(summary = "Get all tasks assigned to current user across all projects")
    public PageResponse<TaskSummaryResponse> getMyTasks(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) UUID statusId,
            @RequestParam(required = false) Task.Priority priority,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        return taskService.getMyTasks(projectId, statusId, priority, page, size);
    }
}