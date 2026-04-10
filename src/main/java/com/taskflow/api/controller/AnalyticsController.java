package com.taskflow.api.controller;

import com.taskflow.api.dto.response.analytics.*;
import com.taskflow.api.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Manager and admin only")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/workload")
    @Operation(summary = "Team workload - active tasks, hours, utilisation per user")
    public WorkloadResponse getWorkload(
            @RequestParam(required = false) UUID workspaceId,
            @RequestParam(required = false) UUID projectId) {
        return analyticsService.getWorkload(workspaceId, projectId);
    }

    @GetMapping("/workload/heatmap")
    @Operation(summary = "Workload heatmap - user × project grid")
    public HeatmapResponse getHeatmap(
            @RequestParam(required = false) UUID workspaceId) {
        return analyticsService.getHeatmap(workspaceId);
    }

    @GetMapping("/projects/{projectId}/summary")
    @Operation(summary = "Project summary - status distribution, completion rate, blockers")
    public ProjectSummaryResponse getProjectSummary(
            @PathVariable UUID projectId) {
        return analyticsService.getProjectSummary(projectId);
    }

    @GetMapping("/projects/{projectId}/velocity")
    @Operation(summary = "Velocity - tasks completed per period")
    public List<VelocityResponse> getVelocity(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "week") String periodType,
            @RequestParam(defaultValue = "12") int periods) {
        return analyticsService.getVelocity(projectId, periodType, periods);
    }

    @GetMapping("/projects/{projectId}/burndown")
    @Operation(summary = "Burndown chart - ideal vs actual remaining tasks")
    public BurndownResponse getBurndown(
            @PathVariable UUID projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return analyticsService.getBurndown(projectId, startDate, endDate);
    }

    @GetMapping("/projects/{projectId}/blockers")
    @Operation(summary = "Active blockers in a project with days blocked")
    public List<BlockerResponse> getBlockers(@PathVariable UUID projectId) {
        return analyticsService.getBlockers(projectId);
    }

    @GetMapping("/users/{userId}/performance")
    @Operation(summary = "Individual performance - accessible by self, manager, admin")
    public UserPerformanceResponse getUserPerformance(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "week") String periodType,
            @RequestParam(defaultValue = "12") int periods,
            @RequestParam(required = false) UUID projectId) {
        return analyticsService.getUserPerformance(userId, periodType, periods, projectId);
    }

    @GetMapping("/team-health")
    @Operation(summary = "Team health score with recommendations")
    public TeamHealthResponse getTeamHealth(
            @RequestParam(required = false) UUID workspaceId,
            @RequestParam(required = false) UUID projectId) {
        return analyticsService.getTeamHealth(workspaceId, projectId);
    }

    @GetMapping("/overview")
    @Operation(summary = "Company overview - admin only")
    public OverviewResponse getOverview() {
        return analyticsService.getOverview();
    }
}