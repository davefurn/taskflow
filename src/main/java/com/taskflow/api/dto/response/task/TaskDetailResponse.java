package com.taskflow.api.dto.response.task;

import com.taskflow.api.dto.response.attachment.AttachmentResponse;
import com.taskflow.api.dto.response.dependency.DependencyResponse;
import com.taskflow.api.dto.response.group.TaskGroupResponse;
import com.taskflow.api.dto.response.label.LabelResponse;
import com.taskflow.api.dto.response.status.TaskStatusResponse;
import com.taskflow.api.dto.response.timeEntry.TimeEntryResponse;
import com.taskflow.api.dto.response.user.UserResponse;
import com.taskflow.api.entity.Task;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TaskDetailResponse {
    private UUID id;
    private String title;
    private String description;
    private TaskStatusResponse status;
    private List<TaskSummaryResponse.AssigneeSummary> assignees;
    private Task.Priority priority;
    private LocalDate dueDate;
    private LocalDate startDate;
    private BigDecimal estimatedHours;
    private Integer weight;
    private TaskGroupResponse taskGroup;
    private List<LabelResponse> labels;
    private TaskDetailResponse parentTask;
    private List<TaskSummaryResponse> subtasks;
    private List<DependencyResponse> dependencies;
    private List<AttachmentResponse> attachments;
    private List<TimeEntryResponse> timeEntries;
    private Instant completedAt;
    private UserResponse createdBy;
    private Instant createdAt;
    private Instant updatedAt;
}