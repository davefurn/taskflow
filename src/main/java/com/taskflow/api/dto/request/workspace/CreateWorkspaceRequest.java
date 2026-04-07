package com.taskflow.api.dto.request.workspace;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateWorkspaceRequest {

    @NotBlank(message = "Workspace name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    private String description;
}