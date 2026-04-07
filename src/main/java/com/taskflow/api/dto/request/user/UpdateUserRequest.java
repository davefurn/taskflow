package com.taskflow.api.dto.request.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 255, message = "Job title must not exceed 255 characters")
    private String jobTitle;

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;

    private String avatarUrl;
}