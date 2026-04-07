package com.taskflow.api.dto.request.project;


import com.taskflow.api.entity.Project;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateProjectRequest {

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    private String description;

    private Project.Status status;

    private LocalDate startDate;

    private LocalDate targetEndDate;

    private UUID leadId;
}