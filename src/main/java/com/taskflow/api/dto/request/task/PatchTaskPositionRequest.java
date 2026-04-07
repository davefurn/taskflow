package com.taskflow.api.dto.request.task;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class PatchTaskPositionRequest {
    @NotNull
    private Integer position;
    private UUID groupId;
}