package com.taskflow.api.service;

import com.taskflow.api.dto.request.dependency.CreateDependencyRequest;
import com.taskflow.api.dto.response.dependency.DependencyResponse;
import com.taskflow.api.entity.*;
import com.taskflow.api.exception.*;
import com.taskflow.api.repository.dependencies.TaskDependencyRepository;
import com.taskflow.api.repository.projects.ProjectMemberRepository;
import com.taskflow.api.repository.tasks.TaskRepository;
import com.taskflow.api.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DependencyService {

    private final TaskDependencyRepository dependencyRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final SecurityUtil securityUtil;

    @Transactional(readOnly = true)
    public List<DependencyResponse> getDependencies(UUID taskId) {
        Task task = getTask(taskId);
        assertProjectMember(task.getProject().getId());
        return dependencyRepository.findAllByTaskId(taskId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public DependencyResponse createDependency(UUID taskId,
                                               CreateDependencyRequest request) {
        Task task = getTask(taskId);
        assertProjectMember(task.getProject().getId());

        Task dependsOn = taskRepository.findById(request.getDependsOnTaskId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task", request.getDependsOnTaskId()));

        // Prevent self-dependency
        if (taskId.equals(request.getDependsOnTaskId())) {
            throw new BadRequestException("A task cannot depend on itself.");
        }

        // Prevent duplicate
        if (dependencyRepository.existsByTaskIdAndDependsOnTaskId(
                taskId, request.getDependsOnTaskId())) {
            throw new ConflictException("This dependency already exists.");
        }

        // Prevent circular dependency
        if (dependencyRepository.existsByTaskIdAndDependsOnTaskId(
                request.getDependsOnTaskId(), taskId)) {
            throw new BadRequestException(
                    "Circular dependency detected. Task B already depends on Task A.");
        }

        TaskDependency dep = TaskDependency.builder()
                .task(task)
                .dependsOnTask(dependsOn)
                .dependencyType(request.getType())
                .build();

        dependencyRepository.save(dep);
        return toResponse(dep);
    }

    @Transactional
    public void deleteDependency(UUID taskId, UUID dependencyId) {
        Task task = getTask(taskId);
        assertProjectMember(task.getProject().getId());

        TaskDependency dep = dependencyRepository.findById(dependencyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dependency", dependencyId));

        dependencyRepository.delete(dep);
    }

    private Task getTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    }

    private void assertProjectMember(UUID projectId) {
        User current = securityUtil.getCurrentUser();
        if (current.getRole() == User.Role.admin) return;
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, current.getId())) {
            throw new ForbiddenException("You are not a member of this project.");
        }
    }

    private DependencyResponse toResponse(TaskDependency d) {
        return DependencyResponse.builder()
                .id(d.getId())
                .taskId(d.getTask().getId())
                .dependsOnTaskId(d.getDependsOnTask().getId())
                .dependsOnTaskTitle(d.getDependsOnTask().getTitle())
                .type(d.getDependencyType())
                .build();
    }
}