package com.taskflow.api.dto.response.comment;
import com.taskflow.api.dto.response.user.UserResponse;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CommentResponse {
    private UUID id;
    private String content;
    private UserResponse user;
    private UUID parentCommentId;
    private List<CommentResponse> replies;
    private Instant createdAt;
    private Instant updatedAt;
}