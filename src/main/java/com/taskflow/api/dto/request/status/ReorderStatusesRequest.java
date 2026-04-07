package com.taskflow.api.dto.request.status;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class ReorderStatusesRequest {
    @NotEmpty(message = "Status IDs are required")
    private List<UUID> statusIds;
}