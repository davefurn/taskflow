package com.taskflow.api.controller;

import com.taskflow.api.dto.request.timeEntry.CreateTimeEntryRequest;
import com.taskflow.api.dto.request.timeEntry.UpdateTimeEntryRequest;
import com.taskflow.api.dto.response.*;
import com.taskflow.api.dto.response.timeEntry.TimeEntryResponse;
import com.taskflow.api.dto.response.timeEntry.TimesheetResponse;
import com.taskflow.api.service.TimeEntryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Time Tracking")
@SecurityRequirement(name = "bearerAuth")
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    @GetMapping("/api/tasks/{taskId}/time-entries")
    public List<TimeEntryResponse> getTimeEntries(@PathVariable UUID taskId) {
        return timeEntryService.getTimeEntries(taskId);
    }

    @PostMapping("/api/tasks/{taskId}/time-entries")
    @ResponseStatus(HttpStatus.CREATED)
    public TimeEntryResponse createTimeEntry(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateTimeEntryRequest request) {
        return timeEntryService.createTimeEntry(taskId, request);
    }

    @PutMapping("/api/time-entries/{entryId}")
    public TimeEntryResponse updateTimeEntry(
            @PathVariable UUID entryId,
            @Valid @RequestBody UpdateTimeEntryRequest request) {
        return timeEntryService.updateTimeEntry(entryId, request);
    }

    @DeleteMapping("/api/time-entries/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTimeEntry(@PathVariable UUID entryId) {
        timeEntryService.deleteTimeEntry(entryId);
    }

    @PostMapping("/api/tasks/{taskId}/timer/start")
    public Map<String, Object> startTimer(@PathVariable UUID taskId) {
        return timeEntryService.startTimer(taskId);
    }

    @PostMapping("/api/tasks/{taskId}/timer/stop")
    public TimeEntryResponse stopTimer(@PathVariable UUID taskId) {
        return timeEntryService.stopTimer(taskId);
    }

    @GetMapping("/api/users/me/timesheet")
    public List<TimesheetResponse> getTimesheet(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return timeEntryService.getTimesheet(startDate, endDate);
    }
}