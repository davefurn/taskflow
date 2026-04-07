package com.taskflow.api.dto.request.group;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateGroupRequest {
    @NotBlank(message = "Group name is required")
    private String name;
}