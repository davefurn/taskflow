package com.taskflow.api.repository.tasks;

import com.taskflow.api.entity.Task;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    // GET /api/projects/{projectId}/tasks — filtered + paginated
// Search is handled separately via searchByText() — never mix tsvector with JPQL
    @Query("""
    SELECT DISTINCT t FROM Task t
    LEFT JOIN t.assignees ta
    LEFT JOIN t.labels    tl
    WHERE t.project.id   = :projectId
    AND   t.parentTask  IS NULL
    AND (:statusId   IS NULL OR t.status.id    = :statusId)
    AND (:assigneeId IS NULL OR ta.user.id     = :assigneeId)
    AND (:priority   IS NULL OR t.priority     = :priority)
    AND (:labelId    IS NULL OR tl.label.id    = :labelId)
    AND (:groupId    IS NULL OR t.taskGroup.id = :groupId)
    AND (:dueBefore  IS NULL OR t.dueDate     <= :dueBefore)
    AND (:dueAfter   IS NULL OR t.dueDate     >= :dueAfter)
""")
    Page<Task> findAllWithFilters(
            @Param("projectId")  UUID projectId,
            @Param("statusId")   UUID statusId,
            @Param("assigneeId") UUID assigneeId,
            @Param("priority")   Task.Priority priority,
            @Param("labelId")    UUID labelId,
            @Param("groupId")    UUID groupId,
            @Param("dueBefore")  LocalDate dueBefore,
            @Param("dueAfter")   LocalDate dueAfter,
            Pageable pageable
    );

    // Full-text search — separate query using native SQL
    @Query(value = """
        SELECT * FROM tasks
        WHERE project_id = :projectId
        AND parent_task_id IS NULL
        AND search_vector @@ plainto_tsquery('english', :search)
        ORDER BY ts_rank(search_vector, plainto_tsquery('english', :search)) DESC
    """, nativeQuery = true)
    Page<Task> searchByText(
            @Param("projectId") UUID projectId,
            @Param("search") String search,
            Pageable pageable
    );

    // GET /api/tasks/{id} — full object
    @Query("""
        SELECT t FROM Task t
        LEFT JOIN FETCH t.status
        LEFT JOIN FETCH t.taskGroup
        LEFT JOIN FETCH t.createdBy
        LEFT JOIN FETCH t.parentTask
        WHERE t.id = :id
    """)
    Optional<Task> findByIdWithDetails(@Param("id") UUID id);

    // GET /api/my-tasks — tasks assigned to current user across all projects
    @Query("""
        SELECT DISTINCT t FROM Task t
        JOIN t.assignees ta
        WHERE ta.user.id = :userId
        AND (:projectId IS NULL OR t.project.id  = :projectId)
        AND (:statusId  IS NULL OR t.status.id   = :statusId)
        AND (:priority  IS NULL OR t.priority    = :priority)
        ORDER BY t.dueDate ASC NULLS LAST
    """)
    Page<Task> findMyTasks(
            @Param("userId")    UUID userId,
            @Param("projectId") UUID projectId,
            @Param("statusId")  UUID statusId,
            @Param("priority")  Task.Priority priority,
            Pageable pageable
    );

    // PATCH /api/tasks/{id}/position
    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.position = :position, t.taskGroup.id = :groupId WHERE t.id = :id")
    void updatePosition(
            @Param("id")       UUID id,
            @Param("position") int position,
            @Param("groupId")  UUID groupId
    );

    // Subtasks — GET /api/tasks/{id} includes these
    List<Task> findAllByParentTaskIdOrderByPositionAsc(UUID parentTaskId);

    // Count subtasks — shown on parent task card
    long countByParentTaskId(UUID parentTaskId);

    // Count completed subtasks
    @Query("SELECT COUNT(t) FROM Task t WHERE t.parentTask.id = :parentTaskId AND t.completedAt IS NOT NULL")
    long countCompletedByParentTaskId(@Param("parentTaskId") UUID parentTaskId);

    // Analytics — tasks due tomorrow (for notification job)
    @Query("""
    SELECT DISTINCT t FROM Task t
    LEFT JOIN FETCH t.project
    JOIN t.assignees ta
    WHERE t.dueDate = :tomorrow
    AND t.completedAt IS NULL
""")
    List<Task> findTasksDueTomorrow(@Param("tomorrow") LocalDate tomorrow);

    // Analytics — overdue tasks
    @Query("""
    SELECT DISTINCT t FROM Task t
    LEFT JOIN FETCH t.project
    JOIN t.assignees ta
    WHERE t.dueDate < :today
    AND t.completedAt IS NULL
""")
    List<Task> findOverdueTasks(@Param("today") LocalDate today);

    // Analytics — tasks completed in period
    @Query("""
        SELECT t FROM Task t
        WHERE t.project.id = :projectId
        AND t.completedAt >= :start
        AND t.completedAt < :end
    """)
    List<Task> findCompletedInPeriod(
            @Param("projectId") UUID projectId,
            @Param("start")     java.time.Instant start,
            @Param("end")       java.time.Instant end
    );

    // Analytics — blocked tasks
    @Query("""
        SELECT DISTINCT t FROM Task t
        JOIN TaskDependency td ON td.task.id = t.id
        WHERE t.project.id = :projectId
        AND t.completedAt IS NULL
        AND td.dependencyType = 'blocked_by'
    """)
    List<Task> findBlockedTasks(@Param("projectId") UUID projectId);

    // Count tasks per project — used in project list response
    long countByProjectId(UUID projectId);

    @Query("""
        SELECT COUNT(t) FROM Task t
        WHERE t.project.id = :projectId
        AND t.completedAt IS NOT NULL
    """)
    long countCompletedByProjectId(@Param("projectId") UUID projectId);

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.taskGroup.id = :newGroupId WHERE t.taskGroup.id = :oldGroupId")
    void reassignGroup(@Param("oldGroupId") UUID oldGroupId,
                       @Param("newGroupId") UUID newGroupId);

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.taskGroup = NULL WHERE t.taskGroup.id = :groupId")
    void ungroupTasks(@Param("groupId") UUID groupId);

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.status.id = :newStatusId WHERE t.status.id = :oldStatusId")
    void reassignStatus(@Param("oldStatusId") UUID oldStatusId,
                        @Param("newStatusId") UUID newStatusId);


    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.parentTask IS NULL")
    List<Task> findAllByProjectIdAndParentTaskIsNull(@Param("projectId") UUID projectId);

    @Query("SELECT t FROM Task t WHERE t.createdAt >= :from AND t.createdAt < :to AND t.project.id = :projectId")
    List<Task> findCreatedInPeriod(@Param("projectId") UUID projectId,
                                   @Param("from") Instant from,
                                   @Param("to") Instant to);

    @Query("SELECT t FROM Task t WHERE t.completedAt IS NOT NULL AND t.completedAt >= :from")
    List<Task> findCompletedAfter(@Param("from") Instant from);

    @Query("SELECT t FROM Task t WHERE t.createdAt >= :from")
    List<Task> findCreatedAfter(@Param("from") Instant from);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.completedAt IS NULL")
    long countActiveTasks();

    @Query("""
    SELECT COUNT(t) FROM Task t
    WHERE t.project.id = :projectId
    AND t.dueDate < :today
    AND t.completedAt IS NULL
""")
    long countOverdueByProjectId(@Param("projectId") UUID projectId,
                                 @Param("today") LocalDate today);
}