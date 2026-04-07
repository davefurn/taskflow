package com.taskflow.api.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class TaskAssigneeId implements Serializable {

    @Column(name = "task_id")
    private UUID taskId;

    @Column(name = "user_id")
    private UUID userId;
}