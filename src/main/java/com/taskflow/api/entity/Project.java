package com.taskflow.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "projects",
        indexes = {
                @Index(name = "idx_projects_workspace", columnList = "workspace_id"),
                @Index(name = "idx_projects_lead",      columnList = "lead_id"),
                @Index(name = "idx_projects_status",    columnList = "status")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Project {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "workspace_id",
            foreignKey = @ForeignKey(name = "fk_projects_workspace")
    )
    private Workspace workspace;

    @Column(nullable = false)
    private String name;

    private String description;

    @Builder.Default
    private String colour = "#6366F1";

    private String icon;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.not_started;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "lead_id",
            foreignKey = @ForeignKey(name = "fk_projects_lead")
    )
    private User lead;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "target_end_date")
    private LocalDate targetEndDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "created_by",
            foreignKey = @ForeignKey(name = "fk_projects_created_by")
    )
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum Status {
        not_started, in_progress, on_hold, completed, archived
    }
}