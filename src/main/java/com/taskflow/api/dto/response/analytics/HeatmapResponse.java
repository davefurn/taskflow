package com.taskflow.api.dto.response.analytics;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class HeatmapResponse {
    private List<UserSummary> users;
    private List<ProjectSummary> projects;
    private List<HeatmapCell> cells;

    @Data @Builder
    public static class UserSummary {
        private UUID userId;
        private String name;
    }

    @Data @Builder
    public static class ProjectSummary {
        private UUID projectId;
        private String name;
    }

    @Data @Builder
    public static class HeatmapCell {
        private UUID userId;
        private UUID projectId;
        private long taskCount;
        private BigDecimal estimatedHours;
    }
}