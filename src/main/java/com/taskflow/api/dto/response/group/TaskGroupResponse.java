package com.taskflow.api.dto.response.group;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class TaskGroupResponse {
    private UUID id;
    private String name;
    private int position;
    private long taskCount;
}