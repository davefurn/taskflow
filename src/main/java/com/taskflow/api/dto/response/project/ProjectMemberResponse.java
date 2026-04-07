package com.taskflow.api.dto.response.project;

import com.taskflow.api.entity.ProjectMember;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProjectMemberResponse {
    private UUID userId;
    private String name;
    private ProjectMember.Role role;
}