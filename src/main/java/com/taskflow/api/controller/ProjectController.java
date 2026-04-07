package com.taskflow.api.controller;


import com.taskflow.api.dto.request.project.AddProjectMembersRequest;
import com.taskflow.api.dto.request.project.CreateProjectRequest;
import com.taskflow.api.dto.request.project.UpdateProjectRequest;
import com.taskflow.api.dto.response.ApiResponse;
import com.taskflow.api.dto.response.project.*;
import com.taskflow.api.dto.response.project.ProjectMemberResponse;
import com.taskflow.api.entity.Project;
import com.taskflow.api.service.ProjectService;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "List projects — filterable by workspace, status, search")
    public List<ProjectResponse> getProjects(
            @RequestParam(required = false) UUID workspaceId,
            @RequestParam(required = false) Project.Status status,
            @RequestParam(required = false) String search) {
        return projectService.getProjects(workspaceId, status, search);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full project details with members and statuses")
    public ProjectDetailResponse getProject(@PathVariable UUID id) {
        return projectService.getProject(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a project — manager and above")
    public ProjectDetailResponse createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        return projectService.createProject(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project — lead or admin")
    public ProjectDetailResponse updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        return projectService.updateProject(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete project — lead or admin")
    public void deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive a project — lead or admin")
    public ApiResponse archiveProject(@PathVariable UUID id) {
        projectService.archiveProject(id);
        return ApiResponse.of("Project archived.");
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "List project members")
    public List<ProjectMemberResponse> getProjectMembers(@PathVariable UUID id) {
        return projectService.getProjectMembers(id);
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add members to project — lead or admin")
    public ApiResponse addProjectMembers(
            @PathVariable UUID id,
            @Valid @RequestBody AddProjectMembersRequest request) {
        int added = projectService.addProjectMembers(id, request);
        return ApiResponse.of("Added " + added + " member(s) to project.");
    }

    @DeleteMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a member from project — lead or admin")
    public void removeProjectMember(
            @PathVariable UUID id,
            @PathVariable UUID userId) {
        projectService.removeProjectMember(id, userId);
    }
}