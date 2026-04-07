package com.taskflow.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "daily_workload_snapshots",
        indexes = {
                @Index(name = "idx_workload_snapshot", columnList = "user_id, snapshot_date")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DailyWorkloadSnapshot {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "fk_workload_snapshot_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "project_id",
            foreignKey = @ForeignKey(name = "fk_workload_snapshot_project")
    )
    private Project project;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Builder.Default
    @Column(name = "active_tasks")
    private Integer activeTasks = 0;

    @Builder.Default
    @Column(name = "assigned_hours", precision = 6, scale = 1)
    private BigDecimal assignedHours = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "assigned_weight")
    private Integer assignedWeight = 0;

    @Builder.Default
    @Column(name = "overdue_tasks")
    private Integer overdueTasks = 0;

    @Builder.Default
    @Column(name = "completed_today")
    private Integer completedToday = 0;

    @Builder.Default
    @Column(name = "hours_logged", precision = 5, scale = 2)
    private BigDecimal hoursLogged = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}