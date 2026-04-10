package com.taskflow.api.service;

import com.taskflow.api.dto.response.attachment.AttachmentResponse;
import com.taskflow.api.dto.response.user.UserResponse;
import com.taskflow.api.entity.*;
import com.taskflow.api.exception.*;
import com.taskflow.api.repository.attachments.AttachmentRepository;
import com.taskflow.api.repository.projects.ProjectMemberRepository;
import com.taskflow.api.repository.tasks.TaskRepository;
import com.taskflow.api.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final FileUploadService fileUploadService;
    private final SecurityUtil securityUtil;

    // GET /api/tasks/{taskId}/attachments

    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachments(UUID taskId) {
        Task task = getTask(taskId);
        assertProjectMember(task.getProject().getId());

        return attachmentRepository.findAllByTaskIdOrderByCreatedAtDesc(taskId)
                .stream().map(this::toResponse).toList();
    }

    //  POST /api/tasks/{taskId}/attachments 

    @Transactional
    public AttachmentResponse uploadAttachment(UUID taskId, MultipartFile file) {
        User current = securityUtil.getCurrentUser();
        Task task = getTask(taskId);
        assertProjectMember(task.getProject().getId());

        // Upload to Cloudinary
        FileUploadService.UploadResult result =
                fileUploadService.uploadAttachment(file);

        // Save metadata to database
        Attachment attachment = Attachment.builder()
                .task(task)
                .uploadedBy(current)
                .fileName(result.fileName())
                .fileUrl(result.url())
                .fileSize(result.fileSize())
                .mimeType(result.mimeType())
                .build();

        attachmentRepository.save(attachment);
        log.info("Attachment uploaded for task {}: {}", taskId, result.fileName());

        return toResponse(attachment);
    }

    //  DELETE /api/attachments/{id}

    @Transactional
    public void deleteAttachment(UUID attachmentId) {
        User current = securityUtil.getCurrentUser();

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attachment", attachmentId));

        // Only uploader or admin can delete
        if (!attachment.getUploadedBy().getId().equals(current.getId())
                && current.getRole() != User.Role.admin) {
            throw new ForbiddenException("You can only delete your own attachments.");
        }

        attachmentRepository.delete(attachment);
        log.info("Attachment deleted: {}", attachmentId);
    }

    //  Avatar upload (PUT /api/users/me)

    public String uploadAvatar(MultipartFile file) {
        FileUploadService.UploadResult result =
                fileUploadService.uploadAvatar(file);
        return result.url();
    }

    private Task getTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    }

    private void assertProjectMember(UUID projectId) {
        User current = securityUtil.getCurrentUser();
        if (current.getRole() == User.Role.admin) return;
        if (!projectMemberRepository.existsByProjectIdAndUserId(
                projectId, current.getId())) {
            throw new ForbiddenException("You are not a member of this project.");
        }
    }

    private AttachmentResponse toResponse(Attachment a) {
        return AttachmentResponse.builder()
                .id(a.getId())
                .fileName(a.getFileName())
                .fileUrl(a.getFileUrl())
                .fileSize(a.getFileSize())
                .mimeType(a.getMimeType())
                .uploadedBy(a.getUploadedBy() != null
                        ? UserResponse.builder()
                        .id(a.getUploadedBy().getId())
                        .name(a.getUploadedBy().getName())
                        .email(a.getUploadedBy().getEmail())
                        .role(a.getUploadedBy().getRole())
                        .avatarUrl(a.getUploadedBy().getAvatarUrl())
                        .build()
                        : null)
                .createdAt(a.getCreatedAt())
                .build();
    }
}