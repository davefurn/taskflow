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
        name = "team_health_history",
        indexes = {
                @Index(name = "idx_health_history", columnList = "workspace_id, score_date")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TeamHealthHistory {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "workspace_id",
            foreignKey = @ForeignKey(name = "fk_health_history_workspace")
    )
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "project_id",
            foreignKey = @ForeignKey(name = "fk_health_history_project")
    )
    private Project project;

    @Column(name = "score_date", nullable = false)
    private LocalDate scoreDate;

    @Column(name = "health_score",     precision = 5, scale = 2) private BigDecimal healthScore;
    @Column(name = "overdue_rate",     precision = 5, scale = 2) private BigDecimal overdueRate;
    @Column(name = "blocked_rate",     precision = 5, scale = 2) private BigDecimal blockedRate;
    @Column(name = "workload_balance", precision = 5, scale = 2) private BigDecimal workloadBalance;
    @Column(name = "velocity_trend",   precision = 5, scale = 2) private BigDecimal velocityTrend;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}