package com.taskflow.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse {
    private String message;

    public static ApiResponse of(String message) {
        return new ApiResponse(message);
    }
}