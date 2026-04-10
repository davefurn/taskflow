package com.taskflow.api.dto.response.analytics;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data @Builder
public class UserPerformanceResponse {
    private Summary summary;
    private List<TrendPoint> trend;
    private CurrentLoad currentLoad;

    @Data @Builder
    public static class Summary {
        private int tasksCompleted;
        private double onTimeRate;
        private BigDecimal avgCycleTimeHours;
        private BigDecimal hoursLogged;
        private double estimationAccuracy;
    }

    @Data @Builder
    public static class TrendPoint {
        private LocalDate periodStart;
        private int tasksCompleted;
        private double onTimeRate;
        private BigDecimal avgCycleTimeHours;
        private BigDecimal hoursLogged;
    }

    @Data @Builder
    public static class CurrentLoad {
        private long activeTasks;
        private BigDecimal assignedHours;
        private long overdueTasks;
    }
}