package com.taskflow.api.dto.request.timeEntry;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateTimeEntryRequest {
    @DecimalMin(value = "0.1", message = "Hours must be at least 0.1")
    private BigDecimal hours;
    private String description;
    private LocalDate date;
}