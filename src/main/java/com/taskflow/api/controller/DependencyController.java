package com.taskflow.api.controller;

import com.taskflow.api.dto.request.dependency.CreateDependencyRequest;
import com.taskflow.api.dto.response.dependency.DependencyResponse;
import com.taskflow.api.service.DependencyService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks/{taskId}/dependencies")
@RequiredArgsConstructor
@Tag(name = "Dependencies")
@SecurityRequirement(name = "bearerAuth")
public class DependencyController {

    private final DependencyService dependencyService;

    @GetMapping
    public List<DependencyResponse> getDependencies(@PathVariable UUID taskId) {
        return dependencyService.getDependencies(taskId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DependencyResponse createDependency(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateDependencyRequest request) {
        return dependencyService.createDependency(taskId, request);
    }

    @DeleteMapping("/{dependencyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDependency(
            @PathVariable UUID taskId,
            @PathVariable UUID dependencyId) {
        dependencyService.deleteDependency(taskId, dependencyId);
    }
}