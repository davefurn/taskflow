package com.taskflow.api.service;


import com.taskflow.api.dto.request.timeEntry.CreateTimeEntryRequest;
import com.taskflow.api.dto.request.timeEntry.UpdateTimeEntryRequest;
import com.taskflow.api.dto.response.*;
import com.taskflow.api.dto.response.timeEntry.TimeEntryResponse;
import com.taskflow.api.dto.response.timeEntry.TimesheetResponse;
import com.taskflow.api.entity.*;
import com.taskflow.api.exception.*;
import com.taskflow.api.repository.projects.ProjectMemberRepository;
import com.taskflow.api.repository.tasks.TaskRepository;
import com.taskflow.api.repository.timeEntriesAndTimers.ActiveTimerRepository;
import com.taskflow.api.repository.timeEntriesAndTimers.TimeEntryRepository;
import com.taskflow.api.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final ActiveTimerRepository activeTimerRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final SecurityUtil securityUtil;

    @Transactional(readOnly = true)
    public List<TimeEntryResponse> getTimeEntries(UUID taskId) {
        Task task = getTask(taskId);
        assertProjectMember(task.getProject().getId());
        return timeEntryRepository.findAllByTaskIdOrderByDateDesc(taskId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public TimeEntryResponse createTimeEntry(UUID taskId,
                                             CreateTimeEntryRequest request) {
        User current = securityUtil.getCurrentUser();
        Task task = getTask(taskId);
        assertProjectMember(task.getProject().getId());

        TimeEntry entry = TimeEntry.builder()
                .task(task)
                .user(current)
                .hours(request.getHours())
                .description(request.getDescription())
                .date(request.getDate())
                .build();

        timeEntryRepository.save(entry);
        return toResponse(entry);
    }

    @Transactional
    public TimeEntryResponse updateTimeEntry(UUID entryId,
                                             UpdateTimeEntryRequest request) {
        User current = securityUtil.getCurrentUser();
        TimeEntry entry = timeEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeEntry", entryId));

        if (!entry.getUser().getId().equals(current.getId())
                && current.getRole() != User.Role.admin) {
            throw new ForbiddenException("You can only edit your own time entries.");
        }

        if (request.getHours() != null)       entry.setHours(request.getHours());
        if (request.getDescription() != null)  entry.setDescription(request.getDescription());
        if (request.getDate() != null)         entry.setDate(request.getDate());

        timeEntryRepository.save(entry);
        return toResponse(entry);
    }

    @Transactional
    public void deleteTimeEntry(UUID entryId) {
        User current = securityUtil.getCurrentUser();
        TimeEntry entry = timeEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeEntry", entryId));

        if (!entry.getUser().getId().equals(current.getId())
                && current.getRole() != User.Role.admin) {
            throw new ForbiddenException("You can only delete your own time entries.");
        }

        timeEntryRepository.delete(entry);
    }

    @Transactional
    public Map<String, Object> startTimer(UUID taskId) {
        User current = securityUtil.getCurrentUser();
        Task task = getTask(taskId);
        assertProjectMember(task.getProject().getId());

        // Enforce one active timer per user
        if (activeTimerRepository.findByUserId(current.getId()).isPresent()) {
            throw new ConflictException(
                    "You already have an active timer running. Stop it before starting a new one.");
        }

        ActiveTimer timer = ActiveTimer.builder()
                .task(task)
                .user(current)
                .startedAt(Instant.now())
                .build();

        activeTimerRepository.save(timer);

        return Map.of(
                "timerId", timer.getId(),
                "startedAt", timer.getStartedAt()
        );
    }

    @Transactional
    public TimeEntryResponse stopTimer(UUID taskId) {
        User current = securityUtil.getCurrentUser();

        ActiveTimer timer = activeTimerRepository
                .findByUserIdAndTaskId(current.getId(), taskId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active timer found for this task."));

        Instant stoppedAt = Instant.now();
        long seconds = ChronoUnit.SECONDS.between(timer.getStartedAt(), stoppedAt);
        BigDecimal hours = BigDecimal.valueOf(seconds).divide(BigDecimal.valueOf(3600), 2,
                java.math.RoundingMode.HALF_UP);

        TimeEntry entry = TimeEntry.builder()
                .task(timer.getTask())
                .user(current)
                .hours(hours)
                .date(LocalDate.now())
                .startedAt(timer.getStartedAt())
                .endedAt(stoppedAt)
                .build();

        timeEntryRepository.save(entry);
        activeTimerRepository.delete(timer);

        return toResponse(entry);
    }

    @Transactional(readOnly = true)
    public List<TimesheetResponse> getTimesheet(LocalDate startDate, LocalDate endDate) {

        User current = securityUtil.getCurrentUser();

        List<TimeEntry> entries = timeEntryRepository.findTimesheetEntries(
                current.getId(), startDate, endDate);

        // Group by date
        Map<LocalDate, List<TimeEntry>> byDate = entries.stream()
                .collect(Collectors.groupingBy(TimeEntry::getDate));

        return byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    List<TimesheetResponse.TimesheetEntry> dayEntries = entry.getValue()
                            .stream().map(te -> TimesheetResponse.TimesheetEntry.builder()
                                    .taskId(te.getTask().getId())
                                    .taskTitle(te.getTask().getTitle())
                                    .projectName(te.getTask().getProject().getName())
                                    .hours(te.getHours())
                                    .build())
                            .toList();

                    BigDecimal total = dayEntries.stream()
                            .map(TimesheetResponse.TimesheetEntry::getHours)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return TimesheetResponse.builder()
                            .date(entry.getKey())
                            .entries(dayEntries)
                            .totalHours(total)
                            .build();
                })
                .toList();
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

    private TimeEntryResponse toResponse(TimeEntry te) {
        return TimeEntryResponse.builder()
                .id(te.getId())
                .userId(te.getUser().getId())
                .hours(te.getHours())
                .description(te.getDescription())
                .date(te.getDate())
                .startedAt(te.getStartedAt())
                .endedAt(te.getEndedAt())
                .build();
    }
}