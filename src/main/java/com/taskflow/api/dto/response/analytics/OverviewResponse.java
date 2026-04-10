package com.taskflow.api.dto.response.analytics;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class OverviewResponse {
    private long totalProjects;
    private long activeProjects;
    private long totalTasks;
    private long activeTasks;
    private long activeUsers;
    private double companyHealthScore;
    private double completionRateThisMonth;
    private List<OverloadedUser> mostOverloaded;
    private List<OverdueProject> mostOverdueProjects;

    @Data @Builder
    public static class OverloadedUser {
        private UUID userId;
        private String name;
        private long activeTasks;
    }

    @Data @Builder
    public static class OverdueProject {
        private UUID projectId;
        private String name;
        private long overdueTasks;
    }
}