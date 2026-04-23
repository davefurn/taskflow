package com.taskflow.api.controller;

import com.taskflow.api.service.BatchJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
@Profile("!prod")
public class DevController {

    private final BatchJobService batchJobService;

    @PostMapping("/batch/due-tomorrow")
    public String triggerDueTomorrow() {
        batchJobService.sendDueTomorrowReminders();
        return "Done — check logs";
    }

    @PostMapping("/batch/overdue")
    public String triggerOverdue() {
        batchJobService.detectOverdueTasks();
        return "Done — check logs";
    }
}