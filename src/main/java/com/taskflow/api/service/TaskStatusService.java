package com.taskflow.api.service;

import com.taskflow.api.dto.request.status.CreateStatusRequest;
import com.taskflow.api.dto.request.status.ReorderStatusesRequest;
import com.taskflow.api.dto.request.status.UpdateStatusRequest;
import com.taskflow.api.dto.response.status.TaskStatusResponse;
import com.taskflow.api.entity.*;
import com.taskflow.api.exception.*;
import com.taskflow.api.repository.projects.ProjectMemberRepository;
import com.taskflow.api.repository.projects.ProjectRepository;
import com.taskflow.api.repository.taskStatusesAndGroups.TaskStatusRepository;
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
public class TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final SecurityUtil securityUtil;

    @Transactional(readOnly = true)
    public List<TaskStatusResponse> getStatuses(UUID projectId) {
        assertProjectExists(projectId);
        return taskStatusRepository.findAllByProjectIdOrderByPositionAsc(projectId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public TaskStatusResponse createStatus(UUID projectId, CreateStatusRequest request) {
        Project project = getProjectAndAssertLead(projectId);

        int nextPosition = taskStatusRepository.findMaxPositionByProjectId(projectId) + 1;

        TaskStatus status = TaskStatus.builder()
                .project(project)
                .name(request.getName().trim())
                .colour(request.getColour())
                .position(nextPosition)
                .isDoneState(request.isDoneState())
                .isDefault(false)
                .build();

        taskStatusRepository.save(status);
        return toResponse(status);
    }

    @Transactional
    public TaskStatusResponse updateStatus(UUID projectId, UUID statusId,
                                           UpdateStatusRequest request) {
        getProjectAndAssertLead(projectId);

        TaskStatus status = taskStatusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Status", statusId));

        if (!status.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Status does not belong to this project.");
        }

        if (request.getName() != null)       status.setName(request.getName().trim());
        if (request.getColour() != null)     status.setColour(request.getColour());
        if (request.getIsDoneState() != null) status.setDoneState(request.getIsDoneState());

        taskStatusRepository.save(status);
        return toResponse(status);
    }

    @Transactional
    public void reorderStatuses(UUID projectId, ReorderStatusesRequest request) {
        getProjectAndAssertLead(projectId);

        List<UUID> ids = request.getStatusIds();
        for (int i = 0; i < ids.size(); i++) {
            taskStatusRepository.updatePosition(ids.get(i), i + 1);
        }
    }

    @Transactional
    public void deleteStatus(UUID projectId, UUID statusId, UUID reassignToId) {
        getProjectAndAssertLead(projectId);

        TaskStatus status = taskStatusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Status", statusId));

        if (!status.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Status does not belong to this project.");
        }

        if (status.isDefault()) {
            throw new BadRequestException("Cannot delete the default status.");
        }

        // Reassign tasks if reassignTo provided
        if (reassignToId != null) {
            TaskStatus target = taskStatusRepository.findById(reassignToId)
                    .orElseThrow(() -> new ResourceNotFoundException("Status", reassignToId));
            taskRepository.reassignStatus(statusId, reassignToId);
        }

        taskStatusRepository.delete(status);
    }

    private Project getProjectAndAssertLead(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        User current = securityUtil.getCurrentUser();
        if (current.getRole() == User.Role.admin) return project;

        projectMemberRepository.findByProjectIdAndUserId(projectId, current.getId())
                .filter(pm -> pm.getRole() == ProjectMember.Role.lead)
                .orElseThrow(() -> new ForbiddenException(
                        "Only the project lead or admin can manage statuses."));

        return project;
    }

    private void assertProjectExists(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
    }

    private TaskStatusResponse toResponse(TaskStatus s) {
        return TaskStatusResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .colour(s.getColour())
                .position(s.getPosition())
                .isDoneState(s.isDoneState())
                .isDefault(s.isDefault())
                .build();
    }
}