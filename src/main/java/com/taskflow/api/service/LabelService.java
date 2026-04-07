package com.taskflow.api.service;

import com.taskflow.api.dto.request.label.AssignLabelsRequest;
import com.taskflow.api.dto.request.label.CreateLabelRequest;
import com.taskflow.api.dto.response.label.LabelResponse;
import com.taskflow.api.entity.*;
import com.taskflow.api.entity.embeddable.TaskLabelId;
import com.taskflow.api.exception.*;
import com.taskflow.api.repository.labels.LabelRepository;
import com.taskflow.api.repository.labels.TaskLabelRepository;
import com.taskflow.api.repository.projects.ProjectMemberRepository;
import com.taskflow.api.repository.projects.ProjectRepository;
import com.taskflow.api.repository.tasks.TaskRepository;
import com.taskflow.api.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;
    private final TaskRepository taskRepository;
    private final TaskLabelRepository taskLabelRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final SecurityUtil securityUtil;

    @Transactional(readOnly = true)
    public List<LabelResponse> getLabels(UUID projectId) {
        assertProjectExists(projectId);
        return labelRepository.findAllByProjectIdOrderByNameAsc(projectId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public LabelResponse createLabel(UUID projectId, CreateLabelRequest request) {
        Project project = getProjectAndAssertMember(projectId);
        Label label = Label.builder()
                .project(project)
                .name(request.getName().trim())
                .colour(request.getColour())
                .build();
        labelRepository.save(label);
        return toResponse(label);
    }

    @Transactional
    public LabelResponse updateLabel(UUID projectId, UUID labelId,
                                     CreateLabelRequest request) {
        getProjectAndAssertMember(projectId);
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label", labelId));
        if (request.getName() != null) label.setName(request.getName().trim());
        if (request.getColour() != null) label.setColour(request.getColour());
        labelRepository.save(label);
        return toResponse(label);
    }

    @Transactional
    public void deleteLabel(UUID projectId, UUID labelId) {
        getProjectAndAssertMember(projectId);
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label", labelId));
        labelRepository.delete(label);
    }

    @Transactional
    public void assignLabels(UUID taskId, AssignLabelsRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        assertProjectMember(task.getProject().getId());

        taskLabelRepository.deleteAllByTaskId(taskId);

        for (UUID labelId : request.getLabelIds()) {
            Label label = labelRepository.findById(labelId)
                    .orElseThrow(() -> new ResourceNotFoundException("Label", labelId));
            taskLabelRepository.save(
                    TaskLabel.builder()
                            .id(new TaskLabelId(taskId, labelId))
                            .task(task)
                            .label(label)
                            .build()
            );
        }
    }

    @Transactional
    public void removeLabel(UUID taskId, UUID labelId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        assertProjectMember(task.getProject().getId());
        taskLabelRepository.deleteByTaskIdAndLabelId(taskId, labelId);
    }

    private Project getProjectAndAssertMember(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
        assertProjectMember(projectId);
        return project;
    }

    private void assertProjectExists(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
    }

    private void assertProjectMember(UUID projectId) {
        User current = securityUtil.getCurrentUser();
        if (current.getRole() == User.Role.admin) return;
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, current.getId())) {
            throw new ForbiddenException("You are not a member of this project.");
        }
    }

    private LabelResponse toResponse(Label l) {
        return LabelResponse.builder()
                .id(l.getId())
                .name(l.getName())
                .colour(l.getColour())
                .build();
    }
}