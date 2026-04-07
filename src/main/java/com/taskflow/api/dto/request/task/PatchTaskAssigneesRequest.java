package com.taskflow.api.dto.request.task;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class PatchTaskAssigneesRequest {
    @NotNull
    private List<UUID> assigneeIds;
}