package com.taskflow.api.controller;

import com.taskflow.api.dto.request.group.CreateGroupRequest;
import com.taskflow.api.dto.request.group.ReorderGroupsRequest;
import com.taskflow.api.dto.request.group.UpdateGroupRequest;
import com.taskflow.api.dto.response.*;
import com.taskflow.api.dto.response.group.TaskGroupResponse;
import com.taskflow.api.service.TaskGroupService;
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
@RequestMapping("/api/projects/{projectId}/groups")
@RequiredArgsConstructor
@Tag(name = "Task Groups")
@SecurityRequirement(name = "bearerAuth")
public class TaskGroupController {

    private final TaskGroupService taskGroupService;

    @GetMapping
    @Operation(summary = "Get all task groups for a project")
    public List<TaskGroupResponse> getGroups(@PathVariable UUID projectId) {
        return taskGroupService.getGroups(projectId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a task group")
    public TaskGroupResponse createGroup(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateGroupRequest request) {
        return taskGroupService.createGroup(projectId, request);
    }

    @PutMapping("/{groupId}")
    @Operation(summary = "Update a task group name")
    public TaskGroupResponse updateGroup(
            @PathVariable UUID projectId,
            @PathVariable UUID groupId,
            @Valid @RequestBody UpdateGroupRequest request) {
        return taskGroupService.updateGroup(projectId, groupId, request);
    }

    @PutMapping("/reorder")
    @Operation(summary = "Reorder groups")
    public ApiResponse reorderGroups(
            @PathVariable UUID projectId,
            @Valid @RequestBody ReorderGroupsRequest request) {
        taskGroupService.reorderGroups(projectId, request);
        return ApiResponse.of("Groups reordered.");
    }

    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a group - optionally move tasks to another group")
    public void deleteGroup(
            @PathVariable UUID projectId,
            @PathVariable UUID groupId,
            @RequestParam(required = false) UUID reassignTo) {
        taskGroupService.deleteGroup(projectId, groupId, reassignTo);
    }
}