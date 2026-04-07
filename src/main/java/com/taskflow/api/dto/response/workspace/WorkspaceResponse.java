package com.taskflow.api.dto.response.workspace;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class WorkspaceResponse {
    private UUID id;
    private String name;
    private String description;
    private long memberCount;
    private long projectCount;
}