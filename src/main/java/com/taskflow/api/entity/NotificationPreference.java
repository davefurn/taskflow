package com.taskflow.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_preferences")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "fk_notification_prefs_user")
    )
    private User user;

    @Builder.Default
    @Column(name = "task_assigned")
    private boolean taskAssigned = true;

    @Builder.Default
    @Column(name = "mentioned_in_comment")
    private boolean mentionedInComment = true;

    @Builder.Default
    @Column(name = "task_due_tomorrow")
    private boolean taskDueTomorrow = true;

    @Builder.Default
    @Column(name = "task_overdue")
    private boolean taskOverdue = true;

    @Builder.Default
    @Column(name = "status_changes")
    private boolean statusChanges = false;

    @Builder.Default
    @Column(name = "weekly_summary")
    private boolean weeklySummary = true;

    @Builder.Default
    @Column(name = "email_enabled")
    private boolean emailEnabled = true;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}