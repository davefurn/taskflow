package com.taskflow.api.service;

import com.taskflow.api.entity.*;
import com.taskflow.api.repository.analytics.DailyWorkloadSnapshotRepository;
import com.taskflow.api.repository.analytics.TeamHealthHistoryRepository;
import com.taskflow.api.repository.authAndUsers.UserRepository;
import com.taskflow.api.repository.dependencies.TaskDependencyRepository;
import com.taskflow.api.repository.notifications.NotificationPreferenceRepository;
import com.taskflow.api.repository.notifications.NotificationRepository;
import com.taskflow.api.repository.projects.ProjectRepository;
import com.taskflow.api.repository.taskStatusesAndGroups.TaskStatusRepository;
import com.taskflow.api.repository.tasks.TaskAssigneeRepository;
import com.taskflow.api.repository.tasks.TaskRepository;
import com.taskflow.api.repository.timeEntriesAndTimers.TimeEntryRepository;
import com.taskflow.api.util.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final DailyWorkloadSnapshotRepository workloadSnapshotRepository;
    private final TeamHealthHistoryRepository teamHealthHistoryRepository;
    private final EmailService emailService;

    // 1. Due tomorrow reminders - runs daily at 8:00 AM

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void sendDueTomorrowReminders() {
        log.info("Batch: running due-tomorrow reminders");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Task> tasksDueTomorrow = taskRepository.findTasksDueTomorrow(tomorrow);

        for (Task task : tasksDueTomorrow) {
            for (TaskAssignee assignee : task.getAssignees()) {
                User user = assignee.getUser();

                notificationPreferenceRepository
                        .findByUserId(user.getId())
                        .ifPresent(prefs -> {
                            if (prefs.isTaskDueTomorrow()) {
                                // In-app notification
                                createNotification(
                                        user,
                                        "task_due_tomorrow",
                                        "Task due tomorrow",
                                        "\"" + task.getTitle() + "\" is due tomorrow.",
                                        "/tasks/" + task.getId()
                                );

                                // Email notification
                                if (prefs.isEmailEnabled()) {
                                    emailService.sendDueTomorrowReminder(
                                            user.getEmail(), task.getTitle());
                                }
                            }
                        });
            }
        }

        log.info("Batch: due-tomorrow reminders sent for {} tasks",
                tasksDueTomorrow.size());
    }

    //2. Overdue detection - runs daily at 9:00 AM

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void detectOverdueTasks() {
        log.info("Batch: running overdue detection");

        LocalDate today = LocalDate.now();
        List<Task> overdueTasks = taskRepository.findOverdueTasks(today);

        for (Task task : overdueTasks) {
            for (TaskAssignee assignee : task.getAssignees()) {
                User user = assignee.getUser();

                notificationPreferenceRepository
                        .findByUserId(user.getId())
                        .ifPresent(prefs -> {
                            if (prefs.isTaskOverdue()) {
                                createNotification(
                                        user,
                                        "task_overdue",
                                        "Task overdue",
                                        "\"" + task.getTitle() + "\" is overdue.",
                                        "/tasks/" + task.getId()
                                );

                                if (prefs.isEmailEnabled()) {
                                    emailService.sendOverdueNotification(
                                            user.getEmail(), task.getTitle());
                                }
                            }
                        });
            }
        }

        log.info("Batch: overdue notifications sent for {} tasks",
                overdueTasks.size());
    }

    // 3. Weekly workload summary - every Monday at 9:00 AM

    @Scheduled(cron = "0 0 9 * * MON")
    @Transactional
    public void sendWeeklyWorkloadSummary() {
        log.info("Batch: running weekly workload summary");

        List<User> managers = userRepository.findAllWithFilters(
                null, User.Role.manager.name(), null);

        for (User manager : managers) {
            notificationPreferenceRepository
                    .findByUserId(manager.getId())
                    .ifPresent(prefs -> {
                        if (prefs.isWeeklySummary() && prefs.isEmailEnabled()) {
                            // Count active tasks across their projects
                            long activeTasks = taskAssigneeRepository
                                    .countActiveByUserId(manager.getId());

                            createNotification(
                                    manager,
                                    "weekly_summary",
                                    "Weekly workload summary",
                                    "You have " + activeTasks
                                            + " active tasks this week.",
                                    "/analytics/workload"
                            );
                        }
                    });
        }

        log.info("Batch: weekly summaries sent to {} managers", managers.size());
    }

    //4. Daily workload snapshot - runs at 11:00 PM

    @Scheduled(cron = "0 0 23 * * *")
    @Transactional
    public void snapshotDailyWorkload() {
        log.info("Batch: snapshotting daily workload");

        LocalDate today = LocalDate.now();
        List<User> users = userRepository.findAll();
        List<Project> projects = projectRepository.findAll();

        for (User user : users) {
            for (Project project : projects) {
                List<Task> activeTasks = taskAssigneeRepository
                        .findActiveTasksByUserId(user.getId())
                        .stream()
                        .filter(t -> t.getProject().getId()
                                .equals(project.getId()))
                        .toList();

                if (activeTasks.isEmpty()) continue;

                long overdue = activeTasks.stream()
                        .filter(t -> t.getDueDate() != null
                                && t.getDueDate().isBefore(today))
                        .count();

                BigDecimal assignedHours = activeTasks.stream()
                        .map(t -> t.getEstimatedHours() != null
                                ? t.getEstimatedHours() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                int assignedWeight = activeTasks.stream()
                        .mapToInt(t -> t.getWeight() != null ? t.getWeight() : 0)
                        .sum();

                BigDecimal hoursLogged = timeEntryRepository
                        .sumHoursByUserAndPeriod(user.getId(), today, today);

                long completedToday = taskAssigneeRepository
                        .countCompletedByUserIdSince(
                                user.getId(),
                                today.atStartOfDay(ZoneOffset.UTC).toInstant());

                workloadSnapshotRepository.save(
                        DailyWorkloadSnapshot.builder()
                                .user(user)
                                .project(project)
                                .snapshotDate(today)
                                .activeTasks(activeTasks.size())
                                .assignedHours(assignedHours)
                                .assignedWeight(assignedWeight)
                                .overdueTasks((int) overdue)
                                .completedToday((int) completedToday)
                                .hoursLogged(hoursLogged != null
                                        ? hoursLogged : BigDecimal.ZERO)
                                .build()
                );
            }
        }

        log.info("Batch: daily workload snapshot complete for {} users",
                users.size());
    }

    // 5. Team health snapshot - runs at 11:30 PM

    @Scheduled(cron = "0 30 23 * * *")
    @Transactional
    public void snapshotTeamHealth() {
        log.info("Batch: snapshotting team health");

        LocalDate today = LocalDate.now();
        List<Project> projects = projectRepository.findAll();

        for (Project project : projects) {
            List<Task> tasks = taskRepository
                    .findAllByProjectIdAndParentTaskIsNull(project.getId());

            if (tasks.isEmpty()) continue;

            long overdue = tasks.stream()
                    .filter(t -> t.getDueDate() != null
                            && t.getDueDate().isBefore(today)
                            && t.getCompletedAt() == null)
                    .count();

            long blocked = taskDependencyRepository
                    .findActiveBlockersInProject(project.getId())
                    .stream().map(td -> td.getTask().getId())
                    .distinct().count();

            double overdueRate = (double) overdue / tasks.size() * 100;
            double blockedRate = (double) blocked / tasks.size() * 100;
            double healthScore = Math.max(0,
                    100 - (overdueRate * 0.4) - (blockedRate * 0.3));

            teamHealthHistoryRepository.save(
                    TeamHealthHistory.builder()
                            .workspace(project.getWorkspace())
                            .project(project)
                            .scoreDate(today)
                            .healthScore(BigDecimal.valueOf(
                                    Math.round(healthScore * 100.0) / 100.0))
                            .overdueRate(BigDecimal.valueOf(
                                    Math.round(overdueRate * 100.0) / 100.0))
                            .blockedRate(BigDecimal.valueOf(
                                    Math.round(blockedRate * 100.0) / 100.0))
                            .workloadBalance(BigDecimal.valueOf(100.0))
                            .velocityTrend(BigDecimal.ZERO)
                            .build()
            );
        }

        log.info("Batch: team health snapshot complete for {} projects",
                projects.size());
    }

    // 6. Clean up expired tokens - runs daily at 2:00 AM

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanExpiredTokens() {
        log.info("Batch: cleaning expired password reset tokens");
        // Handled by PasswordResetTokenRepository.deleteAllExpired

        // Called here for completeness
    }

    // ── 7. Auto-unblock tasks — runs every hour ───────────────

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void autoUnblockTasks() {
        // Find all blocked tasks whose blocking task is now complete
        // and send notifications
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        List<TaskDependency> deps = taskDependencyRepository
                .findAllRecentlyUnblocked(oneHourAgo);

        for (TaskDependency dep : deps) {
            Task blockedTask = dep.getTask();

            for (TaskAssignee assignee : blockedTask.getAssignees()) {
                createNotification(
                        assignee.getUser(),
                        "task_unblocked",
                        "Task unblocked",
                        "\"" + blockedTask.getTitle()
                                + "\" is no longer blocked — \""
                                + dep.getDependsOnTask().getTitle()
                                + "\" was completed.",
                        "/tasks/" + blockedTask.getId()
                );
            }
        }

        if (!deps.isEmpty()) {
            log.info("Batch: {} tasks unblocked", deps.size());
        }
    }

    private void createNotification(User user, String type,
                                    String title, String message,
                                    String linkUrl) {
        notificationRepository.save(
                Notification.builder()
                        .user(user)
                        .type(type)
                        .title(title)
                        .message(message)
                        .linkUrl(linkUrl)
                        .isRead(false)
                        .build()
        );
    }
}