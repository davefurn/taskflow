package com.taskflow.api.dto.request.status;

import lombok.Data;

@Data
public class UpdateStatusRequest {
    private String name;
    private String colour;
    private Boolean isDoneState;
}