package com.taskflow.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(
        name = "task_statuses",
        indexes = {
                @Index(name = "idx_task_statuses_project", columnList = "project_id"),
                @Index(name = "idx_task_statuses_position", columnList = "project_id, position")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TaskStatus {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "project_id",
            foreignKey = @ForeignKey(name = "fk_task_statuses_project")
    )
    private Project project;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    private String colour = "#6B7280";

    @Column(nullable = false)
    private Integer position;

    @Column(name = "is_done_state")
    @Builder.Default
    private boolean isDoneState = false;

    @Column(name = "is_default")
    @Builder.Default
    private boolean isDefault = false;
}