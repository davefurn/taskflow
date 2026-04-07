package com.taskflow.api.dto.response.timeEntry;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TimesheetResponse {
    private LocalDate date;
    private List<TimesheetEntry> entries;
    private BigDecimal totalHours;

    @Data
    @Builder
    public static class TimesheetEntry {
        private UUID taskId;
        private String taskTitle;
        private String projectName;
        private BigDecimal hours;
    }
}