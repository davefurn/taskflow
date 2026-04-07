package com.taskflow.api.dto.request.label;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateLabelRequest {
    @NotBlank(message = "Label name is required")
    private String name;
    private String colour = "#EF4444";
}