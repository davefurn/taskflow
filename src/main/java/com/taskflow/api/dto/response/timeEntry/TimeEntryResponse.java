package com.taskflow.api.dto.response.timeEntry;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class TimeEntryResponse {
    private UUID id;
    private UUID userId;
    private BigDecimal hours;
    private String description;
    private LocalDate date;
    private Instant startedAt;
    private Instant endedAt;
}