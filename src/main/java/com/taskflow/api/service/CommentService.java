package com.taskflow.api.service;

import com.taskflow.api.dto.request.comment.CreateCommentRequest;
import com.taskflow.api.dto.request.comment.UpdateCommentRequest;
import com.taskflow.api.dto.response.*;
import com.taskflow.api.dto.response.comment.CommentResponse;
import com.taskflow.api.dto.response.user.UserResponse;
import com.taskflow.api.entity.*;
import com.taskflow.api.exception.*;
import com.taskflow.api.repository.activityLog.ActivityLogRepository;
import com.taskflow.api.repository.authAndUsers.UserRepository;
import com.taskflow.api.repository.comments.CommentRepository;
import com.taskflow.api.repository.notifications.NotificationPreferenceRepository;
import com.taskflow.api.repository.notifications.NotificationRepository;
import com.taskflow.api.repository.projects.ProjectMemberRepository;
import com.taskflow.api.repository.tasks.TaskRepository;
import com.taskflow.api.security.SecurityUtil;
import com.taskflow.api.util.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.regex.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final EmailService emailService;

    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(UUID taskId) {
        Task task = getTask(taskId);
        assertProjectMember(task.getProject().getId());

        return commentRepository.findTopLevelByTaskId(taskId)
                .stream().map(c -> toResponse(c, true)).toList();
    }

    @Transactional
    public CommentResponse createComment(UUID taskId, CreateCommentRequest request) {

        User current = securityUtil.getCurrentUser();
        Task task = getTask(taskId);
        assertProjectMember(task.getProject().getId());

        Comment parent = null;
        if (request.getParentCommentId() != null) {
            parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Comment", request.getParentCommentId()));
        }

        Comment comment = Comment.builder()
                .task(task)
                .user(current)
                .content(request.getContent())
                .parentComment(parent)
                .build();

        commentRepository.save(comment);

        // Log activity
        activityLogRepository.save(ActivityLog.builder()
                .project(task.getProject())
                .task(task)
                .user(current)
                .action("commented")
                .build());

        // Detect @mentions and notify
        detectAndNotifyMentions(request.getContent(), task, current);

        return toResponse(comment, false);
    }

    @Transactional
    public CommentResponse updateComment(UUID commentId, UpdateCommentRequest request) {

        User current = securityUtil.getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        // Only the author can edit their comment
        if (!comment.getUser().getId().equals(current.getId())
                && current.getRole() != User.Role.admin) {
            throw new ForbiddenException("You can only edit your own comments.");
        }

        comment.setContent(request.getContent());
        commentRepository.save(comment);
        return toResponse(comment, false);
    }

    @Transactional
    public void deleteComment(UUID commentId) {

        User current = securityUtil.getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        if (!comment.getUser().getId().equals(current.getId())
                && current.getRole() != User.Role.admin) {
            throw new ForbiddenException("You can only delete your own comments.");
        }

        commentRepository.delete(comment);
    }

    private void detectAndNotifyMentions(String content, Task task, User author) {
        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            String mentionedName = matcher.group(1);

            // Find user by name in the project
            userRepository.findAllWithFilters(null, null, mentionedName)
                    .stream()
                    .filter(u -> !u.getId().equals(author.getId()))
                    .forEach(mentionedUser -> {

                        // In-app notification
                        notificationRepository.save(
                                Notification.builder()
                                        .user(mentionedUser)
                                        .type("mentioned")
                                        .title(author.getName() + " mentioned you")
                                        .message(author.getName() + " mentioned you in: \""
                                                + task.getTitle() + "\"")
                                        .linkUrl("/tasks/" + task.getId())
                                        .isRead(false)
                                        .build()
                        );

                        // Email
                        notificationPreferenceRepository
                                .findByUserId(mentionedUser.getId())
                                .ifPresent(prefs -> {
                                    if (prefs.isMentionedInComment() && prefs.isEmailEnabled()) {
                                        emailService.sendMentionNotification(
                                                mentionedUser.getEmail(),
                                                author.getName(),
                                                task.getTitle()
                                        );
                                    }
                                });

                        log.info("Notified {} — mentioned in task: {}",
                                mentionedUser.getEmail(), task.getTitle());
                    });
        }
    }

    private Task getTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    }

    private void assertProjectMember(UUID projectId) {
        User current = securityUtil.getCurrentUser();
        if (current.getRole() == User.Role.admin) return;
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, current.getId())) {
            throw new ForbiddenException("You are not a member of this project.");
        }
    }

    private CommentResponse toResponse(Comment c, boolean includeReplies) {
        return CommentResponse.builder()
                .id(c.getId())
                .content(c.getContent())
                .user(UserResponse.builder()
                        .id(c.getUser().getId())
                        .name(c.getUser().getName())
                        .email(c.getUser().getEmail())
                        .role(c.getUser().getRole())
                        .avatarUrl(c.getUser().getAvatarUrl())
                        .build())
                .parentCommentId(c.getParentComment() != null
                        ? c.getParentComment().getId() : null)
                .replies(includeReplies
                        ? commentRepository.findRepliesByParentId(c.getId())
                        .stream().map(r -> toResponse(r, false)).toList()
                        : List.of())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}