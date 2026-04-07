package com.taskflow.api.dto.response.attachment;


import com.taskflow.api.dto.response.user.UserResponse;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AttachmentResponse {
    private UUID id;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String mimeType;
    private UserResponse uploadedBy;
    private Instant createdAt;
}