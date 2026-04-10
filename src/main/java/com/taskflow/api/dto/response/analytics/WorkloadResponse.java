package com.taskflow.api.dto.response.analytics;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class WorkloadResponse {
    private List<WorkloadUserResponse> users;
}