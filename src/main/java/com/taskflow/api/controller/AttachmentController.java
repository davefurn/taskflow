package com.taskflow.api.controller;

import com.taskflow.api.dto.response.attachment.AttachmentResponse;
import com.taskflow.api.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Attachments")
@SecurityRequirement(name = "bearerAuth")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @GetMapping("/api/tasks/{taskId}/attachments")
    @Operation(summary = "List all attachments for a task")
    public List<AttachmentResponse> getAttachments(@PathVariable UUID taskId) {
        return attachmentService.getAttachments(taskId);
    }

    @PostMapping(
            value = "/api/tasks/{taskId}/attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload a file attachment to a task - max 10MB")
    public AttachmentResponse uploadAttachment(
            @PathVariable UUID taskId,
            @RequestParam("file") MultipartFile file) {
        return attachmentService.uploadAttachment(taskId, file);
    }

    @DeleteMapping("/api/attachments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an attachment")
    public void deleteAttachment(@PathVariable UUID id) {
        attachmentService.deleteAttachment(id);
    }
}