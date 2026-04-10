package com.taskflow.api.dto.response.analytics;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder
public class ProjectSummaryResponse {
    private long totalTasks;
    private long completedTasks;
    private long inProgressTasks;
    private long overdueTasks;
    private long blockedTasks;
    private List<StatusDistribution> statusDistribution;
    private BigDecimal avgCycleTimeHours;
    private BigDecimal completionRate;
    private ScopeCreep scopeCreep;

    @Data @Builder
    public static class StatusDistribution {
        private String statusName;
        private long count;
        private double percentage;
    }

    @Data @Builder
    public static class ScopeCreep {
        private long originalScope;
        private long addedDuring;
        private long removedDuring;
    }
}