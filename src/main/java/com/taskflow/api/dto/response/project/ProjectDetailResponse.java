package com.taskflow.api.dto.response.project;

import com.taskflow.api.dto.response.user.UserResponse;
import com.taskflow.api.entity.Project;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProjectDetailResponse {
    private UUID id;
    private String name;
    private String description;
    private Project.Status status;
    private String colour;
    private String icon;
    private UserResponse lead;
    private LocalDate startDate;
    private LocalDate targetEndDate;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ProjectMemberResponse> members;
}