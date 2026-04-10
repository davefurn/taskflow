package com.taskflow.api.repository.dependencies;

import com.taskflow.api.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, UUID> {

    // GET /api/tasks/{taskId}/dependencies
    @Query("""
        SELECT td FROM TaskDependency td
        JOIN FETCH td.dependsOnTask
        WHERE td.task.id = :taskId
    """)
    List<TaskDependency> findAllByTaskId(@Param("taskId") UUID taskId);

    // Prevent circular dependencies before creating
    boolean existsByTaskIdAndDependsOnTaskId(UUID taskId, UUID dependsOnTaskId);

    // When a task completes — find everything it was blocking
    // so we can remove blocked badges and send notifications
    @Query("""
        SELECT td FROM TaskDependency td
        JOIN FETCH td.task
        WHERE td.dependsOnTask.id = :completedTaskId
    """)
    List<TaskDependency> findAllBlockedByTask(@Param("completedTaskId") UUID completedTaskId);

    // GET /api/analytics/projects/{projectId}/blockers
    @Query("""
        SELECT td FROM TaskDependency td
        JOIN FETCH td.task          t
        JOIN FETCH td.dependsOnTask dt
        WHERE t.project.id    = :projectId
        AND   t.completedAt  IS NULL
        AND   dt.completedAt IS NULL
        AND   td.dependencyType = 'blocked_by'
    """)
    List<TaskDependency> findActiveBlockersInProject(@Param("projectId") UUID projectId);
    // For auto-unblock job — find dependencies where blocking task
// was completed in the last hour
    @Query("""
    SELECT td FROM TaskDependency td
    JOIN FETCH td.task t
    JOIN FETCH td.dependsOnTask dt
    WHERE dt.completedAt IS NOT NULL
    AND dt.completedAt >= :oneHourAgo
    AND t.completedAt IS NULL
""")
    List<TaskDependency> findAllRecentlyUnblocked(
            @Param("oneHourAgo") Instant oneHourAgo
    );
}