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
        name = "period_metrics",
        indexes = {
                @Index(name = "idx_period_metrics", columnList = "project_id, period_type, period_start")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PeriodMetric {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "project_id",
            foreignKey = @ForeignKey(name = "fk_period_metrics_project")
    )
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false)
    private PeriodType periodType;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Builder.Default @Column(name = "tasks_completed")  private Integer tasksCompleted  = 0;
    @Builder.Default @Column(name = "weight_completed") private Integer weightCompleted = 0;
    @Builder.Default @Column(name = "tasks_created")    private Integer tasksCreated    = 0;
    @Builder.Default @Column(name = "tasks_overdue")    private Integer tasksOverdue    = 0;
    @Builder.Default @Column(name = "scope_added")      private Integer scopeAdded      = 0;
    @Builder.Default @Column(name = "scope_removed")    private Integer scopeRemoved    = 0;

    @Column(name = "avg_cycle_time_hours",    precision = 8, scale = 2) private BigDecimal avgCycleTimeHours;
    @Column(name = "median_cycle_time_hours", precision = 8, scale = 2) private BigDecimal medianCycleTimeHours;
    @Column(name = "p90_cycle_time_hours",    precision = 8, scale = 2) private BigDecimal p90CycleTimeHours;
    @Column(name = "avg_lead_time_hours",     precision = 8, scale = 2) private BigDecimal avgLeadTimeHours;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public enum PeriodType { week, sprint, month }
}