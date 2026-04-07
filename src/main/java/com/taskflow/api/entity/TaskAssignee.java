package com.taskflow.api.entity;

import com.taskflow.api.entity.embeddable.TaskAssigneeId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "task_assignees",
        indexes = {
                @Index(name = "idx_task_assignees_user", columnList = "user_id"),
                @Index(name = "idx_task_assignees_task", columnList = "task_id")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TaskAssignee {

    @EmbeddedId
    private TaskAssigneeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskId")
    @JoinColumn(
            name = "task_id",
            foreignKey = @ForeignKey(name = "fk_task_assignees_task")
    )
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "fk_task_assignees_user")
    )
    private User user;

    @CreationTimestamp
    @Column(name = "assigned_at", updatable = false)
    private Instant assignedAt;
}