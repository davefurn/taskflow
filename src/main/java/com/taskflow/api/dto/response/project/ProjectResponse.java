package com.taskflow.api.dto.response.project;

import com.taskflow.api.dto.response.user.UserResponse;
import com.taskflow.api.entity.Project;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ProjectResponse {
    private UUID id;
    private String name;
    private Project.Status status;
    private String colour;
    private String icon;
    private UserResponse lead;
    private long memberCount;
    private long taskCount;
    private long completedTaskCount;
    private LocalDate startDate;
    private LocalDate targetEndDate;
}