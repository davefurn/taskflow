package com.taskflow.api.dto.request.status;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateStatusRequest {
    @NotBlank(message = "Status name is required")
    private String name;
    private String colour = "#6B7280";
    private boolean isDoneState = false;
}