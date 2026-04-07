package com.taskflow.api.dto.request.task;


import com.taskflow.api.entity.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    private String description;
    private UUID statusId;
    private List<UUID> assigneeIds;
    private Task.Priority priority = Task.Priority.none;
    private LocalDate dueDate;
    private LocalDate startDate;
    private BigDecimal estimatedHours;
    private Integer weight;
    private UUID groupId;
    private List<UUID> labelIds;
    private UUID parentTaskId;
}