package com.taskflow.api.dto.request.timeEntry;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateTimeEntryRequest {
    @NotNull(message = "Hours is required")
    @DecimalMin(value = "0.1", message = "Hours must be at least 0.1")
    private BigDecimal hours;
    private String description;
    @NotNull(message = "Date is required")
    private LocalDate date;
}