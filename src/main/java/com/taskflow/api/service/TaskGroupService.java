package com.taskflow.api.service;


import com.taskflow.api.dto.request.group.CreateGroupRequest;
import com.taskflow.api.dto.request.group.ReorderGroupsRequest;
import com.taskflow.api.dto.request.group.UpdateGroupRequest;
import com.taskflow.api.dto.response.group.TaskGroupResponse;
import com.taskflow.api.entity.*;
import com.taskflow.api.exception.*;
import com.taskflow.api.repository.projects.ProjectMemberRepository;
import com.taskflow.api.repository.projects.ProjectRepository;
import com.taskflow.api.repository.taskStatusesAndGroups.TaskGroupRepository;
import com.taskflow.api.repository.tasks.TaskRepository;
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
public class TaskGroupService {

    private final TaskGroupRepository taskGroupRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final SecurityUtil securityUtil;

    @Transactional(readOnly = true)
    public List<TaskGroupResponse> getGroups(UUID projectId) {
        assertProjectExists(projectId);
        return taskGroupRepository.findAllByProjectIdOrderByPositionAsc(projectId)
                .stream().map(g -> TaskGroupResponse.builder()
                        .id(g.getId())
                        .name(g.getName())
                        .position(g.getPosition())
                        .taskCount(taskGroupRepository.countTasksByGroupId(g.getId()))
                        .build())
                .toList();
    }

    @Transactional
    public TaskGroupResponse createGroup(UUID projectId, CreateGroupRequest request) {
        Project project = getProjectAndAssertMember(projectId);
        int nextPosition = taskGroupRepository.findMaxPositionByProjectId(projectId) + 1;

        TaskGroup group = TaskGroup.builder()
                .project(project)
                .name(request.getName().trim())
                .position(nextPosition)
                .build();

        taskGroupRepository.save(group);

        return TaskGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .position(group.getPosition())
                .taskCount(0)
                .build();
    }

    @Transactional
    public TaskGroupResponse updateGroup(UUID projectId, UUID groupId,
                                         UpdateGroupRequest request) {
        getProjectAndAssertMember(projectId);

        TaskGroup group = taskGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", groupId));

        group.setName(request.getName().trim());
        taskGroupRepository.save(group);

        return TaskGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .position(group.getPosition())
                .taskCount(taskGroupRepository.countTasksByGroupId(groupId))
                .build();
    }

    @Transactional
    public void reorderGroups(UUID projectId, ReorderGroupsRequest request) {
        getProjectAndAssertMember(projectId);
        List<UUID> ids = request.getGroupIds();
        for (int i = 0; i < ids.size(); i++) {
            taskGroupRepository.updatePosition(ids.get(i), i + 1);
        }
    }

    @Transactional
    public void deleteGroup(UUID projectId, UUID groupId, UUID reassignToId) {
        getProjectAndAssertMember(projectId);

        TaskGroup group = taskGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", groupId));

        // Move tasks to another group or ungroup them
        if (reassignToId != null) {
            taskRepository.reassignGroup(groupId, reassignToId);
        } else {
            taskRepository.ungroupTasks(groupId);
        }

        taskGroupRepository.delete(group);
    }

    private Project getProjectAndAssertMember(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        User current = securityUtil.getCurrentUser();
        if (current.getRole() == User.Role.admin) return project;

        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, current.getId())) {
            throw new ForbiddenException("You are not a member of this project.");
        }
        return project;
    }

    private void assertProjectExists(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
    }
}
