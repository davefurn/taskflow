package com.taskflow.api.dto.response.workspace;

import com.taskflow.api.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class WorkspaceMemberResponse {
    private UUID userId;
    private String name;
    private String email;
    private User.Role role;
}