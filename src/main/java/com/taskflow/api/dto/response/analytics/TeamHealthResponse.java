package com.taskflow.api.dto.response.analytics;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class TeamHealthResponse {
    private double healthScore;
    private double overdueRate;
    private double blockedRate;
    private double workloadBalance;
    private double velocityTrend;
    private List<String> recommendations;
}