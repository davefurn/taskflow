package com.taskflow.api.dto.request.group;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class ReorderGroupsRequest {
    @NotEmpty(message = "Group IDs are required")
    private List<UUID> groupIds;
}