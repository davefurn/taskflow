package com.taskflow.api.service;

import com.taskflow.api.dto.response.*;
import com.taskflow.api.dto.response.activityLog.ActivityLogResponse;
import com.taskflow.api.exception.*;
import com.taskflow.api.repository.activityLog.ActivityLogRepository;
import com.taskflow.api.repository.projects.ProjectMemberRepository;
import com.taskflow.api.repository.projects.ProjectRepository;
import com.taskflow.api.repository.tasks.TaskRepository;
import com.taskflow.api.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final SecurityUtil securityUtil;

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getTaskActivity(UUID taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task", taskId);
        }
        return activityLogRepository.findAllByTaskIdOrderByCreatedAtAsc(taskId)
                .stream().map(a -> ActivityLogResponse.builder()
                        .id(a.getId())
                        .userId(a.getUser() != null ? a.getUser().getId() : null)
                        .userName(a.getUser() != null ? a.getUser().getName() : "System")
                        .action(a.getAction())
                        .fieldChanged(a.getFieldChanged())
                        .oldValue(a.getOldValue())
                        .newValue(a.getNewValue())
                        .createdAt(a.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ActivityLogResponse> getProjectActivity(UUID projectId,
                                                                int page, int size) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }

        var current = securityUtil.getCurrentUser();
        if (current.getRole() != com.taskflow.api.entity.User.Role.admin
                && !projectMemberRepository.existsByProjectIdAndUserId(
                projectId, current.getId())) {
            throw new ForbiddenException("You are not a member of this project.");
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        var result = activityLogRepository
                .findAllByProjectIdOrderByCreatedAtDesc(projectId, pageable);

        return PageResponse.<ActivityLogResponse>builder()
                .content(result.getContent().stream()
                        .map(a -> ActivityLogResponse.builder()
                                .id(a.getId())
                                .userId(a.getUser() != null ? a.getUser().getId() : null)
                                .userName(a.getUser() != null ? a.getUser().getName() : "System")
                                .action(a.getAction())
                                .fieldChanged(a.getFieldChanged())
                                .oldValue(a.getOldValue())
                                .newValue(a.getNewValue())
                                .createdAt(a.getCreatedAt())
                                .build())
                        .toList())
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .build();
    }
}