package com.taskflow.api.dto.request.project;
import com.taskflow.api.entity.ProjectMember;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AddProjectMembersRequest {

    @NotEmpty(message = "At least one user must be specified")
    private List<UUID> userIds;

    private ProjectMember.Role role = ProjectMember.Role.member;
}