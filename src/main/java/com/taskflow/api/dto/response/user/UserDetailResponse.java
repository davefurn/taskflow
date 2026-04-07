package com.taskflow.api.dto.response.user;

import com.taskflow.api.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UserDetailResponse {
    private UUID id;
    private String name;
    private String email;
    private User.Role role;
    private String jobTitle;
    private String avatarUrl;
    private String timezone;
    private Instant lastLogin;
    private List<WorkspaceSummary> workspaces;
    private NotificationPrefsResponse notificationPrefs;

    @Data
    @Builder
    public static class WorkspaceSummary {
        private UUID id;
        private String name;
        private String description;
    }
}