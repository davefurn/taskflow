package com.taskflow.api.dto.request.workspace;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AddWorkspaceMembersRequest {

    @NotEmpty(message = "At least one user must be specified")
    private List<UUID> userIds;
}