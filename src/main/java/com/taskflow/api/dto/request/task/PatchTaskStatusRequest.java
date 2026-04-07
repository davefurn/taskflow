package com.taskflow.api.dto.request.task;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class PatchTaskStatusRequest {
    @NotNull(message = "Status ID is required")
    private UUID statusId;
}