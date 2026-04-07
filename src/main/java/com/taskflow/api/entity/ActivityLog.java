package com.taskflow.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "activity_log",
        indexes = {
                @Index(name = "idx_activity_task",    columnList = "task_id, created_at"),
                @Index(name = "idx_activity_user",    columnList = "user_id, created_at"),
                @Index(name = "idx_activity_project", columnList = "project_id, created_at")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "project_id",
            foreignKey = @ForeignKey(name = "fk_activity_project")
    )
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "task_id",
            foreignKey = @ForeignKey(name = "fk_activity_task")
    )
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "fk_activity_user")
    )
    private User user;

    @Column(nullable = false)
    private String action;

    @Column(name = "field_changed")
    private String fieldChanged;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}