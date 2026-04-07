package com.taskflow.api.dto.request.task;
import com.taskflow.api.entity.Task;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateTaskRequest {

    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    private String description;
    private UUID statusId;
    private Task.Priority priority;
    private LocalDate dueDate;
    private LocalDate startDate;
    private BigDecimal estimatedHours;
    private Integer weight;
    private UUID groupId;
}