package com.taskflow.api.entity;

import com.taskflow.api.entity.embeddable.TaskLabelId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "task_labels",
        indexes = {
                @Index(name = "idx_task_labels_task",  columnList = "task_id"),
                @Index(name = "idx_task_labels_label", columnList = "label_id")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TaskLabel {

    @EmbeddedId
    private TaskLabelId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskId")
    @JoinColumn(
            name = "task_id",
            foreignKey = @ForeignKey(name = "fk_task_labels_task")
    )
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("labelId")
    @JoinColumn(
            name = "label_id",
            foreignKey = @ForeignKey(name = "fk_task_labels_label")
    )
    private Label label;
}