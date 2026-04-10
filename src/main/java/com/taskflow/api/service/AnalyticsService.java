package com.taskflow.api.service;

import com.taskflow.api.dto.response.analytics.*;
import com.taskflow.api.entity.*;
import com.taskflow.api.exception.ForbiddenException;
import com.taskflow.api.exception.ResourceNotFoundException;
import com.taskflow.api.repository.activityLog.ActivityLogRepository;
import com.taskflow.api.repository.authAndUsers.UserRepository;
import com.taskflow.api.repository.dependencies.TaskDependencyRepository;
import com.taskflow.api.repository.projects.ProjectMemberRepository;
import com.taskflow.api.repository.projects.ProjectRepository;
import com.taskflow.api.repository.taskStatusesAndGroups.TaskStatusRepository;
import com.taskflow.api.repository.tasks.TaskAssigneeRepository;
import com.taskflow.api.repository.tasks.TaskRepository;
import com.taskflow.api.repository.timeEntriesAndTimers.TimeEntryRepository;
import com.taskflow.api.repository.workspaces.WorkspaceMemberRepository;
import com.taskflow.api.repository.workspaces.WorkspaceRepository;
import com.taskflow.api.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final SecurityUtil securityUtil;

    // ── GET /api/analytics/workload ───────────────────────────

    @Transactional(readOnly = true)
    public WorkloadResponse getWorkload(UUID workspaceId, UUID projectId) {
        assertManagerOrAdmin();

        List<User> users = resolveUsers(workspaceId, projectId);
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(7);

        List<WorkloadUserResponse> result = users.stream().map(user -> {

            // Active tasks assigned to user
            List<Task> assigned = taskAssigneeRepository
                    .findActiveTasksByUserId(user.getId());

            long overdue = assigned.stream()
                    .filter(t -> t.getDueDate() != null
                            && t.getDueDate().isBefore(today)
                            && t.getCompletedAt() == null)
                    .count();

            long completedThisWeek = taskAssigneeRepository
                    .countCompletedByUserIdSince(user.getId(),
                            weekStart.atStartOfDay(ZoneOffset.UTC).toInstant());

            BigDecimal assignedHours = assigned.stream()
                    .map(t -> t.getEstimatedHours() != null
                            ? t.getEstimatedHours() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int assignedWeight = assigned.stream()
                    .mapToInt(t -> t.getWeight() != null ? t.getWeight() : 0)
                    .sum();

            BigDecimal capacity = user.getWeeklyCapacityHours();
            double utilisation = capacity != null && capacity.compareTo(BigDecimal.ZERO) > 0
                    ? assignedHours.divide(capacity, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue()
                    : 0.0;

            return WorkloadUserResponse.builder()
                    .userId(user.getId())
                    .name(user.getName())
                    .activeTasks(assigned.size())
                    .assignedHours(assignedHours)
                    .assignedWeight(assignedWeight)
                    .capacity(capacity)
                    .utilisationPercent(Math.round(utilisation * 100.0) / 100.0)
                    .overdueTasks((int) overdue)
                    .completedThisWeek((int) completedThisWeek)
                    .build();
        }).toList();

        return WorkloadResponse.builder().users(result).build();
    }

    // ── GET /api/analytics/workload/heatmap ───────────────────

    @Transactional(readOnly = true)
    public HeatmapResponse getHeatmap(UUID workspaceId) {
        assertManagerOrAdmin();

        List<User> users = workspaceId != null
                ? workspaceMemberRepository.findAllByWorkspaceId(workspaceId)
                .stream().map(WorkspaceMember::getUser).toList()
                : userRepository.findAll();

        List<Project> projects = workspaceId != null
                ? projectRepository.findAllByWorkspaceId(workspaceId)
                : projectRepository.findAll();

        List<HeatmapResponse.HeatmapCell> cells = new ArrayList<>();

        for (User user : users) {
            for (Project project : projects) {
                long taskCount = taskAssigneeRepository
                        .countActiveByUserIdAndProjectId(user.getId(), project.getId());
                if (taskCount > 0) {
                    BigDecimal hours = taskAssigneeRepository
                            .sumEstimatedHoursByUserIdAndProjectId(
                                    user.getId(), project.getId());
                    cells.add(HeatmapResponse.HeatmapCell.builder()
                            .userId(user.getId())
                            .projectId(project.getId())
                            .taskCount(taskCount)
                            .estimatedHours(hours != null ? hours : BigDecimal.ZERO)
                            .build());
                }
            }
        }

        return HeatmapResponse.builder()
                .users(users.stream().map(u -> HeatmapResponse.UserSummary.builder()
                        .userId(u.getId()).name(u.getName()).build()).toList())
                .projects(projects.stream().map(p -> HeatmapResponse.ProjectSummary.builder()
                        .projectId(p.getId()).name(p.getName()).build()).toList())
                .cells(cells)
                .build();
    }

    // ── GET /api/analytics/projects/{projectId}/summary ───────

    @Transactional(readOnly = true)
    public ProjectSummaryResponse getProjectSummary(UUID projectId) {
        assertManagerOrAdmin();
        assertProjectMember(projectId);

        List<Task> allTasks = taskRepository.findAllByProjectIdAndParentTaskIsNull(projectId);
        LocalDate today = LocalDate.now();

        long total     = allTasks.size();
        long completed = allTasks.stream()
                .filter(t -> t.getCompletedAt() != null).count();
        long overdue   = allTasks.stream()
                .filter(t -> t.getDueDate() != null
                        && t.getDueDate().isBefore(today)
                        && t.getCompletedAt() == null).count();
        long blocked   = taskDependencyRepository.findActiveBlockersInProject(projectId)
                .stream().map(td -> td.getTask().getId()).distinct().count();
        long inProgress = allTasks.stream()
                .filter(t -> t.getCompletedAt() == null
                        && t.getStatus() != null
                        && !t.getStatus().isDoneState()).count();

        // Status distribution
        List<TaskStatus> statuses =
                taskStatusRepository.findAllByProjectIdOrderByPositionAsc(projectId);
        List<ProjectSummaryResponse.StatusDistribution> dist = statuses.stream()
                .map(s -> {
                    long count = allTasks.stream()
                            .filter(t -> t.getStatus() != null
                                    && t.getStatus().getId().equals(s.getId()))
                            .count();
                    return ProjectSummaryResponse.StatusDistribution.builder()
                            .statusName(s.getName())
                            .count(count)
                            .percentage(total > 0
                                    ? Math.round((double) count / total * 10000) / 100.0 : 0)
                            .build();
                }).toList();

        double completionRate = total > 0
                ? Math.round((double) completed / total * 10000) / 100.0 : 0;

        return ProjectSummaryResponse.builder()
                .totalTasks(total)
                .completedTasks(completed)
                .inProgressTasks(inProgress)
                .overdueTasks(overdue)
                .blockedTasks(blocked)
                .statusDistribution(dist)
                .completionRate(BigDecimal.valueOf(completionRate))
                .scopeCreep(ProjectSummaryResponse.ScopeCreep.builder()
                        .originalScope(total)
                        .addedDuring(0)
                        .removedDuring(0)
                        .build())
                .build();
    }

    // ── GET /api/analytics/projects/{projectId}/velocity ──────

    @Transactional(readOnly = true)
    public List<VelocityResponse> getVelocity(UUID projectId, String periodType,
                                              int periods) {
        assertManagerOrAdmin();

        List<VelocityResponse> result = new ArrayList<>();
        LocalDate end = LocalDate.now();

        for (int i = periods - 1; i >= 0; i--) {
            LocalDate periodEnd   = end.minusWeeks(i);
            LocalDate periodStart = periodEnd.minusWeeks(1);

            Instant from = periodStart.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant to   = periodEnd.atStartOfDay(ZoneOffset.UTC).toInstant();

            List<Task> completed = taskRepository.findCompletedInPeriod(
                    projectId, from, to);
            List<Task> created = taskRepository.findCreatedInPeriod(
                    projectId, from, to);

            int weightCompleted = completed.stream()
                    .mapToInt(t -> t.getWeight() != null ? t.getWeight() : 0).sum();

            result.add(VelocityResponse.builder()
                    .periodStart(periodStart)
                    .periodEnd(periodEnd)
                    .tasksCompleted(completed.size())
                    .weightCompleted(weightCompleted)
                    .tasksCreated(created.size())
                    .build());
        }

        return result;
    }

    // ── GET /api/analytics/projects/{projectId}/burndown ──────

    @Transactional(readOnly = true)
    public BurndownResponse getBurndown(UUID projectId,
                                        LocalDate startDate, LocalDate endDate) {
        assertManagerOrAdmin();

        List<Task> allTasks = taskRepository.findAllByProjectIdAndParentTaskIsNull(projectId);
        long totalScope = allTasks.size();
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);

        List<BurndownResponse.DataPoint> ideal  = new ArrayList<>();
        List<BurndownResponse.DataPoint> actual = new ArrayList<>();
        List<BurndownResponse.DataPoint> scope  = new ArrayList<>();

        for (long day = 0; day <= totalDays; day++) {
            LocalDate date = startDate.plusDays(day);
            Instant dayEnd = date.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

            double idealRemaining = totalScope
                    - ((double) totalScope / totalDays * day);

            long actualCompleted = allTasks.stream()
                    .filter(t -> t.getCompletedAt() != null
                            && !t.getCompletedAt().isAfter(dayEnd))
                    .count();

            ideal.add(BurndownResponse.DataPoint.builder()
                    .date(date)
                    .remainingTasks(Math.round(idealRemaining))
                    .build());

            actual.add(BurndownResponse.DataPoint.builder()
                    .date(date)
                    .remainingTasks(totalScope - actualCompleted)
                    .build());

            scope.add(BurndownResponse.DataPoint.builder()
                    .date(date)
                    .remainingTasks(totalScope)
                    .build());
        }

        return BurndownResponse.builder()
                .ideal(ideal).actual(actual).totalScope(scope)
                .build();
    }

    // ── GET /api/analytics/projects/{projectId}/blockers ──────

    @Transactional(readOnly = true)
    public List<BlockerResponse> getBlockers(UUID projectId) {
        assertManagerOrAdmin();

        return taskDependencyRepository.findActiveBlockersInProject(projectId)
                .stream().map(dep -> {
                    Task blocked   = dep.getTask();
                    Task blocker   = dep.getDependsOnTask();

                    long daysSince = ChronoUnit.DAYS.between(
                            dep.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate(),
                            LocalDate.now());

                    String assigneeName = blocker.getAssignees().stream()
                            .findFirst()
                            .map(ta -> ta.getUser().getName())
                            .orElse("Unassigned");

                    return BlockerResponse.builder()
                            .taskId(blocked.getId())
                            .taskTitle(blocked.getTitle())
                            .blockedByTaskId(blocker.getId())
                            .blockedByTaskTitle(blocker.getTitle())
                            .blockedByAssignee(assigneeName)
                            .blockedSinceDays(daysSince)
                            .build();
                }).toList();
    }

    // ── GET /api/analytics/users/{userId}/performance ─────────

    @Transactional(readOnly = true)
    public UserPerformanceResponse getUserPerformance(UUID userId,
                                                      String periodType,
                                                      int periods,
                                                      UUID projectId) {
        User current = securityUtil.getCurrentUser();

        // Only self, manager, or admin
        if (!current.getId().equals(userId)
                && current.getRole() != User.Role.admin
                && current.getRole() != User.Role.manager) {
            throw new ForbiddenException(
                    "You can only view your own performance data.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        LocalDate today = LocalDate.now();
        LocalDate from  = today.minusWeeks(periods);

        Instant fromInstant = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant   = today.atTime(23,59,59).toInstant(ZoneOffset.UTC);

        List<Task> completed = taskAssigneeRepository
                .findCompletedByUserIdInPeriod(userId, fromInstant, toInstant);

        // On-time rate
        long onTime = completed.stream()
                .filter(t -> t.getDueDate() != null && t.getCompletedAt() != null
                        && !t.getCompletedAt().isAfter(
                        t.getDueDate().atTime(23,59,59).toInstant(ZoneOffset.UTC)))
                .count();

        double onTimeRate = completed.isEmpty() ? 0
                : Math.round((double) onTime / completed.size() * 10000) / 100.0;

        BigDecimal hoursLogged = timeEntryRepository.sumHoursByUserAndPeriod(
                userId, from, today);

        // Build trend — weekly buckets
        List<UserPerformanceResponse.TrendPoint> trend = new ArrayList<>();
        for (int i = periods - 1; i >= 0; i--) {
            LocalDate periodEnd   = today.minusWeeks(i);
            LocalDate periodStart = periodEnd.minusWeeks(1);

            Instant pFrom = periodStart.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant pTo   = periodEnd.atTime(23,59,59).toInstant(ZoneOffset.UTC);

            List<Task> periodCompleted = taskAssigneeRepository
                    .findCompletedByUserIdInPeriod(userId, pFrom, pTo);

            long periodOnTime = periodCompleted.stream()
                    .filter(t -> t.getDueDate() != null && t.getCompletedAt() != null
                            && !t.getCompletedAt().isAfter(
                            t.getDueDate().atTime(23,59,59).toInstant(ZoneOffset.UTC)))
                    .count();

            double periodOnTimeRate = periodCompleted.isEmpty() ? 0
                    : Math.round((double) periodOnTime / periodCompleted.size() * 10000) / 100.0;

            BigDecimal periodHours = timeEntryRepository
                    .sumHoursByUserAndPeriod(userId, periodStart, periodEnd);

            trend.add(UserPerformanceResponse.TrendPoint.builder()
                    .periodStart(periodStart)
                    .tasksCompleted(periodCompleted.size())
                    .onTimeRate(periodOnTimeRate)
                    .hoursLogged(periodHours != null ? periodHours : BigDecimal.ZERO)
                    .build());
        }

        // Current load
        List<Task> activeTasks = taskAssigneeRepository
                .findActiveTasksByUserId(userId);
        long overdueNow = activeTasks.stream()
                .filter(t -> t.getDueDate() != null
                        && t.getDueDate().isBefore(today))
                .count();
        BigDecimal activeHours = activeTasks.stream()
                .map(t -> t.getEstimatedHours() != null
                        ? t.getEstimatedHours() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return UserPerformanceResponse.builder()
                .summary(UserPerformanceResponse.Summary.builder()
                        .tasksCompleted(completed.size())
                        .onTimeRate(onTimeRate)
                        .hoursLogged(hoursLogged != null ? hoursLogged : BigDecimal.ZERO)
                        .estimationAccuracy(0.0)
                        .build())
                .trend(trend)
                .currentLoad(UserPerformanceResponse.CurrentLoad.builder()
                        .activeTasks(activeTasks.size())
                        .assignedHours(activeHours)
                        .overdueTasks(overdueNow)
                        .build())
                .build();
    }

    // ── GET /api/analytics/team-health ────────────────────────

    @Transactional(readOnly = true)
    public TeamHealthResponse getTeamHealth(UUID workspaceId, UUID projectId) {
        assertManagerOrAdmin();

        List<Task> tasks = projectId != null
                ? taskRepository.findAllByProjectIdAndParentTaskIsNull(projectId)
                : getAllTasksInWorkspace(workspaceId);

        if (tasks.isEmpty()) {
            return TeamHealthResponse.builder()
                    .healthScore(100)
                    .overdueRate(0).blockedRate(0)
                    .workloadBalance(100).velocityTrend(0)
                    .recommendations(List.of(
                            "No tasks found. Create tasks to start tracking health."))
                    .build();
        }

        LocalDate today = LocalDate.now();

        double overdueRate = (double) tasks.stream()
                .filter(t -> t.getDueDate() != null
                        && t.getDueDate().isBefore(today)
                        && t.getCompletedAt() == null).count()
                / tasks.size() * 100;

        long blockedCount = projectId != null
                ? taskDependencyRepository.findActiveBlockersInProject(projectId)
                .stream().map(td -> td.getTask().getId()).distinct().count()
                : 0;

        double blockedRate = tasks.isEmpty() ? 0
                : (double) blockedCount / tasks.size() * 100;

        double healthScore = Math.max(0,
                100 - (overdueRate * 0.4) - (blockedRate * 0.3));

        List<String> recommendations = buildRecommendations(
                overdueRate, blockedRate, tasks, workspaceId);

        return TeamHealthResponse.builder()
                .healthScore(Math.round(healthScore * 100.0) / 100.0)
                .overdueRate(Math.round(overdueRate * 100.0) / 100.0)
                .blockedRate(Math.round(blockedRate * 100.0) / 100.0)
                .workloadBalance(100.0)
                .velocityTrend(0.0)
                .recommendations(recommendations)
                .build();
    }

    // ── GET /api/analytics/overview ───────────────────────────

    @Transactional(readOnly = true)
    public OverviewResponse getOverview() {

        User current = securityUtil.getCurrentUser();
        if (current.getRole() != User.Role.admin) {
            throw new ForbiddenException("Only admins can view the company overview.");
        }

        LocalDate today     = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        Instant   monthFrom  = monthStart.atStartOfDay(ZoneOffset.UTC).toInstant();

        long totalProjects  = projectRepository.count();
        long activeProjects = projectRepository.countByStatus(Project.Status.in_progress);
        long totalTasks     = taskRepository.count();
        long activeTasks    = taskRepository.countActiveTasks();
        long activeUsers    = userRepository.count();

        // Completion rate this month
        List<Task> completedThisMonth = taskRepository
                .findCompletedAfter(monthFrom);
        List<Task> createdThisMonth   = taskRepository
                .findCreatedAfter(monthFrom);

        double completionRate = createdThisMonth.isEmpty() ? 0
                : Math.round((double) completedThisMonth.size()
                / createdThisMonth.size() * 10000) / 100.0;

        // Most overloaded users
        List<OverviewResponse.OverloadedUser> overloaded = userRepository.findAll()
                .stream()
                .map(u -> {
                    long count = taskAssigneeRepository
                            .countActiveByUserId(u.getId());
                    return OverviewResponse.OverloadedUser.builder()
                            .userId(u.getId()).name(u.getName())
                            .activeTasks(count).build();
                })
                .filter(u -> u.getActiveTasks() > 0)
                .sorted(Comparator.comparingLong(
                        OverviewResponse.OverloadedUser::getActiveTasks).reversed())
                .limit(5)
                .toList();

        // Most overdue projects
        List<OverviewResponse.OverdueProject> overdueProjects = projectRepository
                .findAll().stream()
                .map(p -> {
                    long count = taskRepository
                            .countOverdueByProjectId(p.getId(), today);
                    return OverviewResponse.OverdueProject.builder()
                            .projectId(p.getId()).name(p.getName())
                            .overdueTasks(count).build();
                })
                .filter(p -> p.getOverdueTasks() > 0)
                .sorted(Comparator.comparingLong(
                        OverviewResponse.OverdueProject::getOverdueTasks).reversed())
                .limit(5)
                .toList();

        return OverviewResponse.builder()
                .totalProjects(totalProjects)
                .activeProjects(activeProjects)
                .totalTasks(totalTasks)
                .activeTasks(activeTasks)
                .activeUsers(activeUsers)
                .companyHealthScore(80.0)
                .completionRateThisMonth(completionRate)
                .mostOverloaded(overloaded)
                .mostOverdueProjects(overdueProjects)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────

    private void assertManagerOrAdmin() {
        User current = securityUtil.getCurrentUser();
        if (current.getRole() != User.Role.admin
                && current.getRole() != User.Role.manager) {
            throw new ForbiddenException(
                    "Only managers and admins can view analytics.");
        }
    }

    private void assertProjectMember(UUID projectId) {
        User current = securityUtil.getCurrentUser();
        if (current.getRole() == User.Role.admin) return;
        if (!projectMemberRepository.existsByProjectIdAndUserId(
                projectId, current.getId())) {
            throw new ForbiddenException("You are not a member of this project.");
        }
    }

    private List<User> resolveUsers(UUID workspaceId, UUID projectId) {
        if (projectId != null) {
            return projectMemberRepository.findAllByProjectId(projectId)
                    .stream().map(ProjectMember::getUser).toList();
        }
        if (workspaceId != null) {
            return workspaceMemberRepository.findAllByWorkspaceId(workspaceId)
                    .stream().map(WorkspaceMember::getUser).toList();
        }
        return userRepository.findAll();
    }

    private List<Task> getAllTasksInWorkspace(UUID workspaceId) {
        if (workspaceId == null) return taskRepository.findAll();
        return projectRepository.findAllByWorkspaceId(workspaceId).stream()
                .flatMap(p -> taskRepository
                        .findAllByProjectIdAndParentTaskIsNull(p.getId()).stream())
                .toList();
    }

    private List<String> buildRecommendations(double overdueRate,
                                              double blockedRate,
                                              List<Task> tasks,
                                              UUID workspaceId) {
        List<String> recs = new ArrayList<>();

        if (overdueRate > 20) {
            recs.add(String.format(
                    "%.0f%% of tasks are overdue - review deadlines or team capacity.",
                    overdueRate));
        }
        if (blockedRate > 10) {
            recs.add(String.format(
                    "%.0f%% of tasks are blocked - resolve dependencies to unblock progress.",
                    blockedRate));
        }
        if (recs.isEmpty()) {
            recs.add("Team health looks good! Keep up the momentum.");
        }
        return recs;
    }
}