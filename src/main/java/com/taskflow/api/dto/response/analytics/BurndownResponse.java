package com.taskflow.api.dto.response.analytics;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data @Builder
public class BurndownResponse {
    private List<DataPoint> ideal;
    private List<DataPoint> actual;
    private List<DataPoint> totalScope;

    @Data @Builder
    public static class DataPoint {
        private LocalDate date;
        private long remainingTasks;
    }
}