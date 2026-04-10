package com.taskflow.api.controller;
import com.taskflow.api.dto.request.workspace.AddWorkspaceMembersRequest;
import com.taskflow.api.dto.request.workspace.CreateWorkspaceRequest;
import com.taskflow.api.dto.request.workspace.UpdateWorkspaceRequest;
import com.taskflow.api.dto.response.*;
import com.taskflow.api.dto.response.workspace.WorkspaceMemberResponse;
import com.taskflow.api.dto.response.workspace.WorkspaceResponse;
import com.taskflow.api.service.WorkspaceService;
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
@RequestMapping("/api/workspaces")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Tag(name = "Workspaces", description = "Workspace management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping
    @Operation(summary = "List workspaces - admins see all, others see their own")
    public List<WorkspaceResponse> getWorkspaces() {
        return workspaceService.getWorkspaces();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a workspace - admin only")
    public WorkspaceResponse createWorkspace(
            @Valid @RequestBody CreateWorkspaceRequest request) {
        return workspaceService.createWorkspace(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a workspace - admin only")
    public WorkspaceResponse updateWorkspace(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWorkspaceRequest request) {
        return workspaceService.updateWorkspace(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a workspace - admin only")
    public void deleteWorkspace(@PathVariable UUID id) {
        workspaceService.deleteWorkspace(id);
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "List workspace members")
    public List<WorkspaceMemberResponse> getMembers(@PathVariable UUID id) {
        return workspaceService.getMembers(id);
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add members to a workspace - admin only")
    public ApiResponse addMembers(
            @PathVariable UUID id,
            @Valid @RequestBody AddWorkspaceMembersRequest request) {
        int added = workspaceService.addMembers(id, request);
        return ApiResponse.of("Added " + added + " member(s) to workspace.");
    }

    @DeleteMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a member from a workspace - admin only")
    public void removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId) {
        workspaceService.removeMember(id, userId);
    }
}