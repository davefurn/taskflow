package com.taskflow.api.repository.labels;

import com.taskflow.api.entity.TaskLabel;
import com.taskflow.api.entity.embeddable.TaskLabelId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskLabelRepository extends JpaRepository<TaskLabel, TaskLabelId> {

    // DELETE /api/tasks/{taskId}/labels/{labelId}
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM TaskLabel tl
        WHERE tl.task.id = :taskId
        AND tl.label.id = :labelId
    """)
    void deleteByTaskIdAndLabelId(
            @Param("taskId")  UUID taskId,
            @Param("labelId") UUID labelId
    );

    // Clear all labels before bulk reassign
    @Modifying
    @Transactional
    @Query("DELETE FROM TaskLabel tl WHERE tl.task.id = :taskId")
    void deleteAllByTaskId(@Param("taskId") UUID taskId);
}