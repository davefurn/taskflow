package com.taskflow.api.dto.response.status;


import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class TaskStatusResponse {
    private UUID id;
    private String name;
    private String colour;
    private int position;
    private boolean isDoneState;
    private boolean isDefault;
}