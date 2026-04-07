package com.taskflow.api.controller;

import com.taskflow.api.dto.request.comment.CreateCommentRequest;
import com.taskflow.api.dto.request.comment.UpdateCommentRequest;
import com.taskflow.api.dto.response.*;
import com.taskflow.api.dto.response.comment.CommentResponse;
import com.taskflow.api.service.CommentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Comments")
@SecurityRequirement(name = "bearerAuth")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/api/tasks/{taskId}/comments")
    public List<CommentResponse> getComments(@PathVariable UUID taskId) {
        return commentService.getComments(taskId);
    }

    @PostMapping("/api/tasks/{taskId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateCommentRequest request) {
        return commentService.createComment(taskId, request);
    }

    @PutMapping("/api/comments/{commentId}")
    public CommentResponse updateComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody UpdateCommentRequest request) {
        return commentService.updateComment(commentId, request);
    }

    @DeleteMapping("/api/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable UUID commentId) {
        commentService.deleteComment(commentId);
    }
}