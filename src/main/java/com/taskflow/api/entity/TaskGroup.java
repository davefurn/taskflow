package com.taskflow.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "task_groups",
        indexes = {
                @Index(name = "idx_task_groups_project", columnList = "project_id")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TaskGroup {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "project_id",
            foreignKey = @ForeignKey(name = "fk_task_groups_project")
    )
    private Project project;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer position;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}