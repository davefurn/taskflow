package com.taskflow.api.service;

import com.taskflow.api.dto.request.workspace.*;
import com.taskflow.api.dto.response.workspace.*;
import com.taskflow.api.entity.*;
import com.taskflow.api.entity.embeddable.WorkspaceMemberId;
import com.taskflow.api.exception.*;
import com.taskflow.api.repository.authAndUsers.UserRepository;
import com.taskflow.api.repository.projects.ProjectRepository;
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
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    // GET /api/workspaces

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getWorkspaces() {

        User current = securityUtil.getCurrentUser();

        // Admins see all workspaces, others see only their own
        List<Workspace> workspaces = current.getRole() == User.Role.admin
                ? workspaceRepository.findAll()
                : workspaceRepository.findAllByUserId(current.getId());

        return workspaces.stream()
                .map(w -> WorkspaceResponse.builder()
                        .id(w.getId())
                        .name(w.getName())
                        .description(w.getDescription())
                        .memberCount(workspaceMemberRepository.countByWorkspaceId(w.getId()))
                        .projectCount(projectRepository.countByWorkspaceId(w.getId()))
                        .build())
                .toList();
    }

    // POST /api/workspaces

    @Transactional
    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request) {

        // Admin only
        User current = securityUtil.getCurrentUser();
        if (current.getRole() != User.Role.admin) {
            throw new ForbiddenException("Only admins can create workspaces.");
        }

        Workspace workspace = Workspace.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();

        workspaceRepository.save(workspace);

        // Auto-add the creator as a member
        workspaceMemberRepository.save(
                WorkspaceMember.builder()
                        .id(new WorkspaceMemberId(workspace.getId(), current.getId()))
                        .workspace(workspace)
                        .user(current)
                        .build()
        );

        log.info("Workspace created: {} by {}", workspace.getName(), current.getEmail());

        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .memberCount(1)
                .projectCount(0)
                .build();
    }

    // PUT /api/workspaces/{id}

    @Transactional
    public WorkspaceResponse updateWorkspace(UUID id, UpdateWorkspaceRequest request) {

        User current = securityUtil.getCurrentUser();
        if (current.getRole() != User.Role.admin) {
            throw new ForbiddenException("Only admins can update workspaces.");
        }

        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", id));

        if (request.getName() != null)
            workspace.setName(request.getName().trim());
        if (request.getDescription() != null)
            workspace.setDescription(request.getDescription());

        workspaceRepository.save(workspace);

        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .memberCount(workspaceMemberRepository.countByWorkspaceId(id))
                .projectCount(projectRepository.countByWorkspaceId(id))
                .build();
    }

    // DELETE /api/workspaces/{id}

    @Transactional
    public void deleteWorkspace(UUID id) {

        User current = securityUtil.getCurrentUser();
        if (current.getRole() != User.Role.admin) {
            throw new ForbiddenException("Only admins can delete workspaces.");
        }

        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", id));

        // Delete members first — Hibernate doesn't follow ON DELETE CASCADE
        workspaceMemberRepository.deleteAllByWorkspaceId(id);

        workspaceRepository.delete(workspace);
        log.info("Workspace deleted: {}", workspace.getName());
    }

    // GET /api/workspaces/{id}/members

    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponse> getMembers(UUID id) {

        if (!workspaceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Workspace", id);
        }

        // Must be a member or admin to view members
        User current = securityUtil.getCurrentUser();
        if (current.getRole() != User.Role.admin
                && !workspaceMemberRepository.existsByWorkspaceIdAndUserId(id, current.getId())) {
            throw new ForbiddenException("You are not a member of this workspace.");
        }

        return workspaceMemberRepository.findAllByWorkspaceId(id)
                .stream()
                .map(wm -> WorkspaceMemberResponse.builder()
                        .userId(wm.getUser().getId())
                        .name(wm.getUser().getName())
                        .email(wm.getUser().getEmail())
                        .role(wm.getUser().getRole())
                        .build())
                .toList();
    }

    // POST /api/workspaces/{id}/members

    @Transactional
    public int addMembers(UUID id, AddWorkspaceMembersRequest request) {

        User current = securityUtil.getCurrentUser();
        if (current.getRole() != User.Role.admin) {
            throw new ForbiddenException("Only admins can add workspace members.");
        }

        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", id));

        int added = 0;
        for (UUID userId : request.getUserIds()) {

            // Skip if already a member
            if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(id, userId)) {
                continue;
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));

            workspaceMemberRepository.save(
                    WorkspaceMember.builder()
                            .id(new WorkspaceMemberId(id, userId))
                            .workspace(workspace)
                            .user(user)
                            .build()
            );
            added++;
        }

        log.info("{} members added to workspace {}", added, workspace.getName());
        return added;
    }

    // DELETE /api/workspaces/{id}/members/{userId}

    @Transactional
    public void removeMember(UUID workspaceId, UUID userId) {

        User current = securityUtil.getCurrentUser();
        if (current.getRole() != User.Role.admin) {
            throw new ForbiddenException("Only admins can remove workspace members.");
        }

        if (!workspaceRepository.existsById(workspaceId)) {
            throw new ResourceNotFoundException("Workspace", workspaceId);
        }

        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new ResourceNotFoundException("User is not a member of this workspace.");
        }

        workspaceMemberRepository.deleteByWorkspaceIdAndUserId(workspaceId, userId);
        log.info("User {} removed from workspace {}", userId, workspaceId);
    }
}