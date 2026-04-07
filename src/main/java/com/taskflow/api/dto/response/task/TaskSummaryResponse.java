package com.taskflow.api.dto.response.task;

import com.taskflow.api.dto.response.label.LabelResponse;
import com.taskflow.api.dto.response.status.TaskStatusResponse;
import com.taskflow.api.entity.Task;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TaskSummaryResponse {
    private UUID id;
    private String title;
    private TaskStatusResponse status;
    private List<AssigneeSummary> assignees;
    private Task.Priority priority;
    private LocalDate dueDate;
    private BigDecimal estimatedHours;
    private Integer weight;
    private List<LabelResponse> labels;
    private long subtaskCount;
    private long completedSubtaskCount;
    private long commentCount;
    private boolean isBlocked;

    @Data
    @Builder
    public static class AssigneeSummary {
        private UUID id;
        private String name;
        private String avatarUrl;
    }
}