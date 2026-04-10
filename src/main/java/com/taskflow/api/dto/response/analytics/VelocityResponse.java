package com.taskflow.api.dto.response.analytics;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data @Builder
public class VelocityResponse {
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private int tasksCompleted;
    private int weightCompleted;
    private int tasksCreated;
}