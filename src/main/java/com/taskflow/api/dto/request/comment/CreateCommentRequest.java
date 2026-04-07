package com.taskflow.api.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateCommentRequest {
    @NotBlank(message = "Comment content is required")
    private String content;
    private UUID parentCommentId;
}