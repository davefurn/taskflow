package com.taskflow.api.service;

import com.taskflow.api.dto.request.project.*;
import com.taskflow.api.dto.response.project.*;
import com.taskflow.api.entity.*;
import com.taskflow.api.entity.embeddable.ProjectMemberId;
import com.taskflow.api.exception.*;
import com.taskflow.api.repository.authAndUsers.UserRepository;
import com.taskflow.api.repository.projects.ProjectMemberRepository;
import com.taskflow.api.repository.projects.ProjectRepository;
import com.taskflow.api.repository.taskStatusesAndGroups.TaskStatusRepository;
import com.taskflow.api.repository.tasks.TaskRepository;
import com.taskflow.api.repository.workspaces.WorkspaceMemberRepository;
import com.taskflow.api.repository.workspaces.WorkspaceRepository;
import com.taskflow.api.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final SecurityUtil securityUtil;
    private final UserService userService;

    // GET /api/projects 

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjects(UUID workspaceId, Project.Status status,
                                             String search) {
        User current = securityUtil.getCurrentUser();

        return projectRepository
                .findAllWithFilters(current.getId(), workspaceId, status, search)
                .stream()
                .map(this::toProjectResponse)
                .toList();
    }

    //GET /api/projects/{id} 

    @Transactional(readOnly = true)
    public ProjectDetailResponse getProject(UUID id) {

        Project project = projectRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));

        assertMemberOrAdmin(project);

        List<ProjectMember> members = projectMemberRepository.findAllByProjectId(id);

        return ProjectDetailResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .colour(project.getColour())
                .icon(project.getIcon())
                .lead(project.getLead() != null
                        ? userService.toUserResponse(project.getLead()) : null)
                .startDate(project.getStartDate())
                .targetEndDate(project.getTargetEndDate())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .members(members.stream()
                        .map(pm -> ProjectMemberResponse.builder()
                                .userId(pm.getUser().getId())
                                .name(pm.getUser().getName())
                                .role(pm.getRole())
                                .build())
                        .toList())
                .build();
    }

    // POST /api/projects 

    @Transactional
    public ProjectDetailResponse createProject(CreateProjectRequest request) {

        User current = securityUtil.getCurrentUser();

        // Manager or above
        if (current.getRole() == User.Role.member
                || current.getRole() == User.Role.viewer) {
            throw new ForbiddenException("Only managers and admins can create projects.");
        }

        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workspace", request.getWorkspaceId()));

        // Must be a member of the workspace
        if (current.getRole() != User.Role.admin
                && !workspaceMemberRepository.existsByWorkspaceIdAndUserId(
                workspace.getId(), current.getId())) {
            throw new ForbiddenException("You are not a member of this workspace.");
        }

        User lead = null;
        if (request.getLeadId() != null) {
            lead = userRepository.findById(request.getLeadId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User", request.getLeadId()));
        }

        Project project = Project.builder()
                .workspace(workspace)
                .name(request.getName().trim())
                .description(request.getDescription())
                .colour(request.getColour() != null ? request.getColour() : "#6366F1")
                .icon(request.getIcon())
                .status(Project.Status.not_started)
                .lead(lead)
                .startDate(request.getStartDate())
                .targetEndDate(request.getTargetEndDate())
                .createdBy(current)
                .build();

        projectRepository.save(project);

        // Create default task statuses — PRD specifies these defaults
        createDefaultStatuses(project);

        // Add creator as lead member
        projectMemberRepository.save(
                ProjectMember.builder()
                        .id(new ProjectMemberId(project.getId(), current.getId()))
                        .project(project)
                        .user(current)
                        .role(ProjectMember.Role.lead)
                        .build()
        );

        log.info("Project created: {} in workspace {}", project.getName(), workspace.getName());
        return getProject(project.getId());
    }

    //PUT /api/projects/{id} 

    @Transactional
    public ProjectDetailResponse updateProject(UUID id, UpdateProjectRequest request) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));

        assertLeadOrAdmin(project);

        if (request.getName() != null)
            project.setName(request.getName().trim());
        if (request.getDescription() != null)
            project.setDescription(request.getDescription());
        if (request.getStatus() != null)
            project.setStatus(request.getStatus());
        if (request.getStartDate() != null)
            project.setStartDate(request.getStartDate());
        if (request.getTargetEndDate() != null)
            project.setTargetEndDate(request.getTargetEndDate());
        if (request.getLeadId() != null) {
            User lead = userRepository.findById(request.getLeadId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User", request.getLeadId()));
            project.setLead(lead);
        }

        projectRepository.save(project);
        return getProject(id);
    }

    // DELETE /api/projects/{id} 

    @Transactional
    public void deleteProject(UUID id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));

        assertLeadOrAdmin(project);
        projectRepository.delete(project);
        log.info("Project deleted: {}", project.getName());
    }

    // POST /api/projects/{id}/archive

    @Transactional
    public void archiveProject(UUID id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));

        assertLeadOrAdmin(project);
        project.setStatus(Project.Status.archived);
        projectRepository.save(project);
        log.info("Project archived: {}", project.getName());
    }

    // GET /api/projects/{id}/members

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getProjectMembers(UUID id) {

        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project", id);
        }

        assertMemberOrAdmin(projectRepository.findById(id).get());

        return projectMemberRepository.findAllByProjectId(id)
                .stream()
                .map(pm -> ProjectMemberResponse.builder()
                        .userId(pm.getUser().getId())
                        .name(pm.getUser().getName())
                        .role(pm.getRole())
                        .build())
                .toList();
    }

    // POST /api/projects/{id}/members

    @Transactional
    public int addProjectMembers(UUID id, AddProjectMembersRequest request) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));

        assertLeadOrAdmin(project);

        int added = 0;
        for (UUID userId : request.getUserIds()) {

            // Skip if already a member
            if (projectMemberRepository.existsByProjectIdAndUserId(id, userId)) {
                continue;
            }

            // User must be a workspace member first
            if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(
                    project.getWorkspace().getId(), userId)) {
                throw new BadRequestException(
                        "User " + userId + " is not a member of this workspace. " +
                                "Add them to the workspace first."
                );
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));

            projectMemberRepository.save(
                    ProjectMember.builder()
                            .id(new ProjectMemberId(id, userId))
                            .project(project)
                            .user(user)
                            .role(request.getRole())
                            .build()
            );
            added++;
        }

        log.info("{} members added to project {}", added, project.getName());
        return added;
    }

    // DELETE /api/projects/{id}/members/{userId}

    @Transactional
    public void removeProjectMember(UUID projectId, UUID userId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        assertLeadOrAdmin(project);

        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ResourceNotFoundException("User is not a member of this project.");
        }

        projectMemberRepository.deleteByProjectIdAndUserId(projectId, userId);
        log.info("User {} removed from project {}", userId, project.getName());
    }

    // Helpers

    private void createDefaultStatuses(Project project) {
        String[][] defaults = {
                {"Backlog",     "#6B7280", "false", "true"},
                {"To Do",       "#3B82F6", "false", "false"},
                {"In Progress", "#F59E0B", "false", "false"},
                {"In Review",   "#8B5CF6", "false", "false"},
                {"Done",        "#10B981", "true",  "false"}
        };

        for (int i = 0; i < defaults.length; i++) {
            taskStatusRepository.save(
                    TaskStatus.builder()
                            .project(project)
                            .name(defaults[i][0])
                            .colour(defaults[i][1])
                            .position(i + 1)
                            .isDoneState(Boolean.parseBoolean(defaults[i][2]))
                            .isDefault(Boolean.parseBoolean(defaults[i][3]))
                            .build()
            );
        }
    }

    private void assertMemberOrAdmin(Project project) {
        User current = securityUtil.getCurrentUser();
        if (current.getRole() == User.Role.admin) return;
        if (!projectMemberRepository.existsByProjectIdAndUserId(
                project.getId(), current.getId())) {
            throw new ForbiddenException("You are not a member of this project.");
        }
    }

    private void assertLeadOrAdmin(Project project) {
        User current = securityUtil.getCurrentUser();
        if (current.getRole() == User.Role.admin) return;

        projectMemberRepository.findByProjectIdAndUserId(project.getId(), current.getId())
                .filter(pm -> pm.getRole() == ProjectMember.Role.lead)
                .orElseThrow(() -> new ForbiddenException(
                        "Only the project lead or an admin can perform this action."));
    }

    private ProjectResponse toProjectResponse(Project p) {
        return ProjectResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .status(p.getStatus())
                .colour(p.getColour())
                .icon(p.getIcon())
                .lead(p.getLead() != null ? userService.toUserResponse(p.getLead()) : null)
                .memberCount(projectMemberRepository.countByProjectId(p.getId()))
                .taskCount(taskRepository.countByProjectId(p.getId()))
                .completedTaskCount(taskRepository.countCompletedByProjectId(p.getId()))
                .startDate(p.getStartDate())
                .targetEndDate(p.getTargetEndDate())
                .build();
    }
}