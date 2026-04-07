package com.taskflow.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "active_timers",
        indexes = {
                @Index(name = "idx_active_timers_task", columnList = "task_id")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ActiveTimer {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "task_id",
            foreignKey = @ForeignKey(name = "fk_active_timers_task")
    )
    private Task task;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            unique = true,
            foreignKey = @ForeignKey(name = "fk_active_timers_user")
    )
    private User user;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;
}