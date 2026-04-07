package com.taskflow.api.service;

import com.taskflow.api.dto.request.task.*;
import com.taskflow.api.dto.response.*;
import com.taskflow.api.dto.response.dependency.DependencyResponse;
import com.taskflow.api.dto.response.group.TaskGroupResponse;
import com.taskflow.api.dto.response.label.LabelResponse;
import com.taskflow.api.dto.response.status.TaskStatusResponse;
import com.taskflow.api.dto.response.task.TaskDetailResponse;
import com.taskflow.api.dto.response.task.TaskSummaryResponse;
import com.taskflow.api.dto.response.user.UserResponse;
import com.taskflow.api.entity.*;
import com.taskflow.api.entity.embeddable.TaskAssigneeId;
import com.taskflow.api.entity.embeddable.TaskLabelId;
import com.taskflow.api.exception.*;
import com.taskflow.api.repository.activityLog.ActivityLogRepository;
import com.taskflow.api.repository.authAndUsers.UserRepository;
import com.taskflow.api.repository.comments.CommentRepository;
import com.taskflow.api.repository.dependencies.TaskDependencyRepository;
import com.taskflow.api.repository.labels.LabelRepository;
import com.taskflow.api.repository.labels.TaskLabelRepository;
import com.taskflow.api.repository.projects.ProjectMemberRepository;
import com.taskflow.api.repository.projects.ProjectRepository;
import com.taskflow.api.repository.taskStatusesAndGroups.TaskGroupRepository;
import com.taskflow.api.repository.taskStatusesAndGroups.TaskStatusRepository;
import com.taskflow.api.repository.tasks.TaskAssigneeRepository;
import com.taskflow.api.repository.tasks.TaskRepository;
import com.taskflow.api.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final TaskGroupRepository taskGroupRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final TaskLabelRepository taskLabelRepository;
    private final LabelRepository labelRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final CommentRepository commentRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    // ── GET /api/projects/{projectId}/tasks ───────────────────

    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> getTasks(
            UUID projectId, UUID statusId, UUID assigneeId,
            Task.Priority priority, UUID labelId, UUID groupId,
            LocalDate dueBefore, LocalDate dueAfter,
            String search, String sort, String order,
            int page, int size) {

        assertProjectMember(projectId);

        Pageable pageable = buildPageable(sort, order, page, size);

        Page<Task> tasks = (search != null && !search.isBlank())
                ? taskRepository.searchByText(projectId, search, pageable)
                : taskRepository.findAllWithFilters(
                projectId, statusId, assigneeId, priority,
                labelId, groupId, dueBefore, dueAfter, pageable);

        return PageResponse.<TaskSummaryResponse>builder()
                .content(tasks.getContent().stream()
                        .map(this::toTaskSummary).toList())
                .totalPages(tasks.getTotalPages())
                .totalElements(tasks.getTotalElements())
                .build();
    }

    // ── GET /api/tasks/{id} ───────────────────────────────────

    @Transactional(readOnly = true)
    public TaskDetailResponse getTask(UUID id) {
        Task task = taskRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
        assertProjectMember(task.getProject().getId());
        return toTaskDetail(task);
    }

    // ── POST /api/projects/{projectId}/tasks ──────────────────

    @Transactional
    public TaskDetailResponse createTask(UUID projectId, CreateTaskRequest request) {

        User current = securityUtil.getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        assertProjectMember(projectId);

        // Resolve status — use default if not specified
        TaskStatus status = request.getStatusId() != null
                ? taskStatusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Status", request.getStatusId()))
                : taskStatusRepository.findByProjectIdAndIsDefaultTrue(projectId)
                .orElseGet(() ->
                        taskStatusRepository.findAllByProjectIdOrderByPositionAsc(projectId)
                                .stream().findFirst()
                                .orElseThrow(() -> new BadRequestException(
                                        "Project has no statuses configured.")));

        // Resolve group
        TaskGroup group = null;
        if (request.getGroupId() != null) {
            group = taskGroupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Group", request.getGroupId()));
        }

        // Resolve parent task (subtasks)
        Task parentTask = null;
        if (request.getParentTaskId() != null) {
            parentTask = taskRepository.findById(request.getParentTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Task", request.getParentTaskId()));
            // Enforce one level deep — PRD requirement
            if (parentTask.getParentTask() != null) {
                throw new BadRequestException(
                        "Sub-subtasks are not allowed. Subtasks can only be one level deep.");
            }
        }

        Task task = Task.builder()
                .project(project)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .status(status)
                .priority(request.getPriority() != null
                        ? request.getPriority() : Task.Priority.none)
                .dueDate(request.getDueDate())
                .startDate(request.getStartDate())
                .estimatedHours(request.getEstimatedHours())
                .weight(request.getWeight())
                .taskGroup(group)
                .parentTask(parentTask)
                .createdBy(current)
                .build();

        taskRepository.save(task);

        // Assign users
        if (request.getAssigneeIds() != null) {
            assignUsers(task, request.getAssigneeIds(), project);
        }

        // Assign labels
        if (request.getLabelIds() != null) {
            assignLabels(task, request.getLabelIds(), projectId);
        }

        // Log activity
        logActivity(project, task, current, "created", null, null, null);

        // Notify assignees
        if (request.getAssigneeIds() != null) {
            notifyAssignees(task, request.getAssigneeIds(), current.getId());
        }

        return toTaskDetail(taskRepository.findByIdWithDetails(task.getId()).get());
    }

    // ── PUT /api/tasks/{id} ───────────────────────────────────

    @Transactional
    public TaskDetailResponse updateTask(UUID id, UpdateTaskRequest request) {

        User current = securityUtil.getCurrentUser();
        Task task = taskRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        assertProjectMember(task.getProject().getId());

        String oldStatus = task.getStatus() != null ? task.getStatus().getName() : null;

        if (request.getTitle() != null)
            task.setTitle(request.getTitle().trim());
        if (request.getDescription() != null)
            task.setDescription(request.getDescription());
        if (request.getPriority() != null)
            task.setPriority(request.getPriority());
        if (request.getDueDate() != null)
            task.setDueDate(request.getDueDate());
        if (request.getStartDate() != null)
            task.setStartDate(request.getStartDate());
        if (request.getEstimatedHours() != null)
            task.setEstimatedHours(request.getEstimatedHours());
        if (request.getWeight() != null)
            task.setWeight(request.getWeight());

        if (request.getStatusId() != null) {
            TaskStatus newStatus = taskStatusRepository.findById(request.getStatusId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Status", request.getStatusId()));

            String newStatusName = newStatus.getName();

            // Mark completed_at when moved to done state
            if (newStatus.isDoneState() && task.getCompletedAt() == null) {
                task.setCompletedAt(Instant.now());
            } else if (!newStatus.isDoneState()) {
                task.setCompletedAt(null);
            }

            task.setStatus(newStatus);

            // Log status change — critical for cycle time analytics
            logActivity(task.getProject(), task, current,
                    "status_changed", "status", oldStatus, newStatusName);
        }

        if (request.getGroupId() != null) {
            TaskGroup group = taskGroupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Group", request.getGroupId()));
            task.setTaskGroup(group);
        }

        taskRepository.save(task);
        return toTaskDetail(taskRepository.findByIdWithDetails(id).get());
    }

    // ── PATCH /api/tasks/{id}/status ──────────────────────────

    @Transactional
    public TaskDetailResponse patchStatus(UUID id, PatchTaskStatusRequest request) {
        UpdateTaskRequest update = new UpdateTaskRequest();
        update.setStatusId(request.getStatusId());
        return updateTask(id, update);
    }

    // ── PATCH /api/tasks/{id}/assignees ───────────────────────

    @Transactional
    public TaskDetailResponse patchAssignees(UUID id, PatchTaskAssigneesRequest request) {

        User current = securityUtil.getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        assertProjectMember(task.getProject().getId());

        // Remove all existing assignees
        taskAssigneeRepository.deleteAllByTaskId(id);

        // Add new assignees
        assignUsers(task, request.getAssigneeIds(), task.getProject());
        notifyAssignees(task, request.getAssigneeIds(), current.getId());

        logActivity(task.getProject(), task, current,
                "assigned", "assignee", null, String.join(", ",
                        request.getAssigneeIds().stream().map(UUID::toString).toList()));

        return toTaskDetail(taskRepository.findByIdWithDetails(id).get());
    }

    // ── PATCH /api/tasks/{id}/position ────────────────────────

    @Transactional
    public void patchPosition(UUID id, PatchTaskPositionRequest request) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task", id);
        }
        taskRepository.updatePosition(id, request.getPosition(), request.getGroupId());
    }

    // ── DELETE /api/tasks/{id} ────────────────────────────────

    @Transactional
    public void deleteTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
        assertProjectMember(task.getProject().getId());
        taskRepository.delete(task);
    }

    // ── POST /api/tasks/{id}/duplicate ────────────────────────

    @Transactional
    public TaskDetailResponse duplicateTask(UUID id) {

        User current = securityUtil.getCurrentUser();
        Task original = taskRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        assertProjectMember(original.getProject().getId());

        Task copy = Task.builder()
                .project(original.getProject())
                .title("Copy of " + original.getTitle())
                .description(original.getDescription())
                .status(original.getStatus())
                .priority(original.getPriority())
                .dueDate(original.getDueDate())
                .startDate(original.getStartDate())
                .estimatedHours(original.getEstimatedHours())
                .weight(original.getWeight())
                .taskGroup(original.getTaskGroup())
                .createdBy(current)
                .build();

        taskRepository.save(copy);
        logActivity(original.getProject(), copy, current, "created", null, null, null);

        return toTaskDetail(taskRepository.findByIdWithDetails(copy.getId()).get());
    }

    // ── GET /api/my-tasks ─────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> getMyTasks(
            UUID projectId, UUID statusId, Task.Priority priority,
            int page, int size) {

        User current = securityUtil.getCurrentUser();
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Task> tasks = taskRepository.findMyTasks(
                current.getId(), projectId, statusId, priority, pageable);

        return PageResponse.<TaskSummaryResponse>builder()
                .content(tasks.getContent().stream()
                        .map(this::toTaskSummary).toList())
                .totalPages(tasks.getTotalPages())
                .totalElements(tasks.getTotalElements())
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────

    private void assignUsers(Task task, List<UUID> userIds, Project project) {
        for (UUID userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));
            taskAssigneeRepository.save(
                    TaskAssignee.builder()
                            .id(new TaskAssigneeId(task.getId(), userId))
                            .task(task)
                            .user(user)
                            .build()
            );
        }
    }

    private void assignLabels(Task task, List<UUID> labelIds, UUID projectId) {
        for (UUID labelId : labelIds) {
            if (!labelRepository.existsByIdAndProjectId(labelId, projectId)) {
                throw new BadRequestException(
                        "Label " + labelId + " does not belong to this project.");
            }
            Label label = labelRepository.findById(labelId)
                    .orElseThrow(() -> new ResourceNotFoundException("Label", labelId));
            taskLabelRepository.save(
                    TaskLabel.builder()
                            .id(new TaskLabelId(task.getId(), labelId))
                            .task(task)
                            .label(label)
                            .build()
            );
        }
    }

    private void notifyAssignees(Task task, List<UUID> assigneeIds, UUID assignedById) {
        // Exclude the person who assigned — don't notify yourself
        assigneeIds.stream()
                .filter(uid -> !uid.equals(assignedById))
                .forEach(uid -> userRepository.findById(uid).ifPresent(user -> {
                    log.info("Notify {} — assigned to task: {}", user.getEmail(), task.getTitle());
                    // EmailService notification handled by notification service in Stream 5
                }));
    }

    private void logActivity(Project project, Task task, User user,
                             String action, String field,
                             String oldVal, String newVal) {
        activityLogRepository.save(
                ActivityLog.builder()
                        .project(project)
                        .task(task)
                        .user(user)
                        .action(action)
                        .fieldChanged(field)
                        .oldValue(oldVal)
                        .newValue(newVal)
                        .build()
        );
    }

    private void assertProjectMember(UUID projectId) {
        User current = securityUtil.getCurrentUser();
        if (current.getRole() == User.Role.admin) return;
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, current.getId())) {
            throw new ForbiddenException("You are not a member of this project.");
        }
    }

    private Pageable buildPageable(String sort, String order, int page, int size) {
        Sort.Direction direction = "desc".equalsIgnoreCase(order)
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        String sortField = switch (sort != null ? sort : "") {
            case "due_date"    -> "dueDate";
            case "priority"    -> "priority";
            case "created_at"  -> "createdAt";
            case "title"       -> "title";
            default            -> "createdAt";
        };

        return PageRequest.of(page - 1, size, Sort.by(direction, sortField));
    }

    public TaskSummaryResponse toTaskSummary(Task t) {
        List<TaskDependency> deps = taskDependencyRepository.findAllByTaskId(t.getId());
        boolean isBlocked = deps.stream().anyMatch(d ->
                d.getDependencyType() == TaskDependency.DependencyType.blocked_by
                        && d.getDependsOnTask().getCompletedAt() == null);

        return TaskSummaryResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .status(t.getStatus() != null ? TaskStatusResponse.builder()
                        .id(t.getStatus().getId())
                        .name(t.getStatus().getName())
                        .colour(t.getStatus().getColour())
                        .position(t.getStatus().getPosition())
                        .isDoneState(t.getStatus().isDoneState())
                        .isDefault(t.getStatus().isDefault())
                        .build() : null)
                .assignees(t.getAssignees().stream()
                        .map(ta -> TaskSummaryResponse.AssigneeSummary.builder()
                                .id(ta.getUser().getId())
                                .name(ta.getUser().getName())
                                .avatarUrl(ta.getUser().getAvatarUrl())
                                .build())
                        .toList())
                .priority(t.getPriority())
                .dueDate(t.getDueDate())
                .estimatedHours(t.getEstimatedHours())
                .weight(t.getWeight())
                .labels(t.getLabels().stream()
                        .map(tl -> LabelResponse.builder()
                                .id(tl.getLabel().getId())
                                .name(tl.getLabel().getName())
                                .colour(tl.getLabel().getColour())
                                .build())
                        .toList())
                .subtaskCount(taskRepository.countByParentTaskId(t.getId()))
                .completedSubtaskCount(taskRepository.countCompletedByParentTaskId(t.getId()))
                .commentCount(commentRepository.countByTaskId(t.getId()))
                .isBlocked(isBlocked)
                .build();
    }

    public TaskDetailResponse toTaskDetail(Task t) {
        return TaskDetailResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .description(t.getDescription())
                .status(t.getStatus() != null ? TaskStatusResponse.builder()
                        .id(t.getStatus().getId())
                        .name(t.getStatus().getName())
                        .colour(t.getStatus().getColour())
                        .position(t.getStatus().getPosition())
                        .isDoneState(t.getStatus().isDoneState())
                        .isDefault(t.getStatus().isDefault())
                        .build() : null)
                .assignees(t.getAssignees().stream()
                        .map(ta -> TaskSummaryResponse.AssigneeSummary.builder()
                                .id(ta.getUser().getId())
                                .name(ta.getUser().getName())
                                .avatarUrl(ta.getUser().getAvatarUrl())
                                .build())
                        .toList())
                .priority(t.getPriority())
                .dueDate(t.getDueDate())
                .startDate(t.getStartDate())
                .estimatedHours(t.getEstimatedHours())
                .weight(t.getWeight())
                .taskGroup(t.getTaskGroup() != null ? TaskGroupResponse.builder()
                        .id(t.getTaskGroup().getId())
                        .name(t.getTaskGroup().getName())
                        .position(t.getTaskGroup().getPosition())
                        .build() : null)
                .labels(t.getLabels().stream()
                        .map(tl -> LabelResponse.builder()
                                .id(tl.getLabel().getId())
                                .name(tl.getLabel().getName())
                                .colour(tl.getLabel().getColour())
                                .build())
                        .toList())
                .subtasks(t.getSubtasks().stream()
                        .map(this::toTaskSummary).toList())
                .dependencies(taskDependencyRepository.findAllByTaskId(t.getId())
                        .stream().map(d -> DependencyResponse.builder()
                                .id(d.getId())
                                .taskId(d.getTask().getId())
                                .dependsOnTaskId(d.getDependsOnTask().getId())
                                .dependsOnTaskTitle(d.getDependsOnTask().getTitle())
                                .type(d.getDependencyType())
                                .build())
                        .toList())
                .completedAt(t.getCompletedAt())
                .createdBy(t.getCreatedBy() != null ? UserResponse.builder()
                        .id(t.getCreatedBy().getId())
                        .name(t.getCreatedBy().getName())
                        .email(t.getCreatedBy().getEmail())
                        .role(t.getCreatedBy().getRole())
                        .build() : null)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}