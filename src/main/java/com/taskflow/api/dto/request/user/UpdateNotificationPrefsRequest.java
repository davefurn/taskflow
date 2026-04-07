package com.taskflow.api.dto.request.user;

import lombok.Data;

@Data
public class UpdateNotificationPrefsRequest {
    private Boolean taskAssigned;
    private Boolean mentionedInComment;
    private Boolean taskDueTomorrow;
    private Boolean taskOverdue;
    private Boolean statusChanges;
    private Boolean weeklySummary;
    private Boolean emailEnabled;
}