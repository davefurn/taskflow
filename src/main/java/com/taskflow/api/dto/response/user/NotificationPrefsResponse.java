package com.taskflow.api.dto.response.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationPrefsResponse {
    private boolean taskAssigned;
    private boolean mentionedInComment;
    private boolean taskDueTomorrow;
    private boolean taskOverdue;
    private boolean statusChanges;
    private boolean weeklySummary;
    private boolean emailEnabled;
}