package com.taskflow.api.dto.request.dependency;


import com.taskflow.api.entity.TaskDependency;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateDependencyRequest {
    @NotNull(message = "Depends on task ID is required")
    private UUID dependsOnTaskId;
    private TaskDependency.DependencyType type = TaskDependency.DependencyType.blocked_by;
}