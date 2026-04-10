package com.taskflow.api.repository.tasks;

import com.taskflow.api.entity.Task;
import com.taskflow.api.entity.TaskAssignee;
import com.taskflow.api.entity.embeddable.TaskAssigneeId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TaskAssigneeRepository extends JpaRepository<TaskAssignee, TaskAssigneeId> {
    @Modifying
    @Transactional
    @Query("DELETE FROM TaskAssignee ta WHERE ta.task.id = :taskId")
    void deleteAllByTaskId(@Param("taskId") UUID taskId);


    @Query("""
        SELECT t FROM Task t
        JOIN t.assignees ta
        WHERE ta.user.id = :userId
        AND t.completedAt IS NULL
    """)
    List<Task> findActiveTasksByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT COUNT(t) FROM Task t
        JOIN t.assignees ta
        WHERE ta.user.id = :userId
        AND t.completedAt IS NOT NULL
        AND t.completedAt >= :since
    """)
    long countCompletedByUserIdSince(@Param("userId") UUID userId,
                                     @Param("since") Instant since);

    @Query("""
        SELECT t FROM Task t
        JOIN t.assignees ta
        WHERE ta.user.id = :userId
        AND t.completedAt IS NOT NULL
        AND t.completedAt >= :from
        AND t.completedAt <= :to
    """)
    List<Task> findCompletedByUserIdInPeriod(@Param("userId") UUID userId,
                                             @Param("from") Instant from,
                                             @Param("to") Instant to);

    @Query("""
        SELECT COUNT(t) FROM Task t
        JOIN t.assignees ta
        WHERE ta.user.id = :userId
        AND t.completedAt IS NULL
    """)
    long countActiveByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT COUNT(t) FROM Task t
        JOIN t.assignees ta
        WHERE ta.user.id = :userId
        AND t.project.id = :projectId
        AND t.completedAt IS NULL
    """)
    long countActiveByUserIdAndProjectId(@Param("userId") UUID userId,
                                         @Param("projectId") UUID projectId);

    @Query("""
        SELECT COALESCE(SUM(t.estimatedHours), 0) FROM Task t
        JOIN t.assignees ta
        WHERE ta.user.id = :userId
        AND t.project.id = :projectId
        AND t.completedAt IS NULL
    """)
    BigDecimal sumEstimatedHoursByUserIdAndProjectId(@Param("userId") UUID userId,
                                                     @Param("projectId") UUID projectId);
}