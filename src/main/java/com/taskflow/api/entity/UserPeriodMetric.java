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
        name = "user_period_metrics",
        indexes = {
                @Index(name = "idx_user_metrics", columnList = "user_id, period_type, period_start")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserPeriodMetric {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "fk_user_metrics_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "project_id",
            foreignKey = @ForeignKey(name = "fk_user_metrics_project")
    )
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false)
    private PeriodMetric.PeriodType periodType;

    @Column(name = "period_start", nullable = false) private LocalDate periodStart;
    @Column(name = "period_end",   nullable = false) private LocalDate periodEnd;

    @Builder.Default @Column(name = "tasks_completed") private Integer tasksCompleted = 0;
    @Builder.Default @Column(name = "tasks_assigned")  private Integer tasksAssigned  = 0;
    @Builder.Default @Column(name = "on_time_count")   private Integer onTimeCount    = 0;
    @Builder.Default @Column(name = "overdue_count")   private Integer overdueCount   = 0;

    @Column(name = "avg_cycle_time_hours", precision = 8, scale = 2) private BigDecimal avgCycleTimeHours;

    @Builder.Default @Column(name = "hours_logged",    precision = 6, scale = 2) private BigDecimal hoursLogged    = BigDecimal.ZERO;
    @Builder.Default @Column(name = "hours_estimated", precision = 6, scale = 2) private BigDecimal hoursEstimated = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}