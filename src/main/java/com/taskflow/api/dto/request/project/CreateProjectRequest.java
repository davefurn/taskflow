package com.taskflow.api.dto.request.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateProjectRequest {

    @NotNull(message = "Workspace is required")
    private UUID workspaceId;

    @NotBlank(message = "Project name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    private String description;

    @Size(max = 7, message = "Colour must be a valid hex code")
    private String colour;

    private String icon;

    private LocalDate startDate;

    private LocalDate targetEndDate;

    private UUID leadId;
}
