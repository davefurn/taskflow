package com.taskflow.api.dto.response.dependency;

import com.taskflow.api.entity.TaskDependency;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class DependencyResponse {
    private UUID id;
    private UUID taskId;
    private UUID dependsOnTaskId;
    private String dependsOnTaskTitle;
    private TaskDependency.DependencyType type;
}