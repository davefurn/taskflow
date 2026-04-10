package com.taskflow.api.dto.response.analytics;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data @Builder
public class WorkloadUserResponse {
    private UUID userId;
    private String name;
    private int activeTasks;
    private BigDecimal assignedHours;
    private int assignedWeight;
    private BigDecimal capacity;
    private double utilisationPercent;
    private int overdueTasks;
    private int completedThisWeek;
}