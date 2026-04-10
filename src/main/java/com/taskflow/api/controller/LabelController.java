package com.taskflow.api.controller;

import com.taskflow.api.dto.request.label.AssignLabelsRequest;
import com.taskflow.api.dto.request.label.CreateLabelRequest;
import com.taskflow.api.dto.response.*;
import com.taskflow.api.dto.response.label.LabelResponse;
import com.taskflow.api.service.LabelService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Labels")
@SecurityRequirement(name = "bearerAuth")
public class LabelController {

    private final LabelService labelService;

    @GetMapping("/api/projects/{projectId}/labels")
    public List<LabelResponse> getLabels(@PathVariable UUID projectId) {
        return labelService.getLabels(projectId);
    }

    @PostMapping("/api/projects/{projectId}/labels")
    @ResponseStatus(HttpStatus.CREATED)
    public LabelResponse createLabel(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateLabelRequest request) {
        return labelService.createLabel(projectId, request);
    }

    @PutMapping("/api/projects/{projectId}/labels/{labelId}")
    public LabelResponse updateLabel(
            @PathVariable UUID projectId,
            @PathVariable UUID labelId,
            @Valid @RequestBody CreateLabelRequest request) {
        return labelService.updateLabel(projectId, labelId, request);
    }

    @DeleteMapping("/api/projects/{projectId}/labels/{labelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLabel(
            @PathVariable UUID projectId,
            @PathVariable UUID labelId) {
        labelService.deleteLabel(projectId, labelId);
    }

    @PostMapping("/api/tasks/{taskId}/labels")
    @Operation(summary = "Assign labels to a task - replaces existing labels")
    public ApiResponse assignLabels(
            @PathVariable UUID taskId,
            @Valid @RequestBody AssignLabelsRequest request) {
        labelService.assignLabels(taskId, request);
        return ApiResponse.of("Labels assigned.");
    }

    @DeleteMapping("/api/tasks/{taskId}/labels/{labelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLabel(
            @PathVariable UUID taskId,
            @PathVariable UUID labelId) {
        labelService.removeLabel(taskId, labelId);
    }
}