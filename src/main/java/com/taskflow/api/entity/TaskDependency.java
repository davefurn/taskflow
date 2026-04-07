package com.taskflow.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "task_dependencies",
        indexes = {
                @Index(name = "idx_task_deps_task",       columnList = "task_id"),
                @Index(name = "idx_task_deps_depends_on", columnList = "depends_on_task_id")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TaskDependency {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "task_id",
            foreignKey = @ForeignKey(name = "fk_task_deps_task")
    )
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "depends_on_task_id",
            foreignKey = @ForeignKey(name = "fk_task_deps_depends_on")
    )
    private Task dependsOnTask;

    @Enumerated(EnumType.STRING)
    @Column(name = "dependency_type")
    @Builder.Default
    private DependencyType dependencyType = DependencyType.blocked_by;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public enum DependencyType {
        blocked_by, related_to
    }
}