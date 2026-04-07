package com.taskflow.api.repository.analytics;

import com.taskflow.api.entity.DailyWorkloadSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DailyWorkloadSnapshotRepository extends JpaRepository<DailyWorkloadSnapshot, UUID> {

    // GET /api/analytics/workload
    @Query("""
        SELECT d FROM DailyWorkloadSnapshot d
        JOIN FETCH d.user
        WHERE d.snapshotDate = :date
        AND (:workspaceId IS NULL OR d.project.workspace.id = :workspaceId)
        AND (:projectId   IS NULL OR d.project.id = :projectId)
    """)
    List<DailyWorkloadSnapshot> findLatestSnapshot(
            @Param("date")        LocalDate date,
            @Param("workspaceId") UUID workspaceId,
            @Param("projectId")   UUID projectId
    );

    // GET /api/analytics/team-health/history
    @Query("""
        SELECT d FROM DailyWorkloadSnapshot d
        WHERE d.user.id = :userId
        AND d.snapshotDate >= :from
        ORDER BY d.snapshotDate ASC
    """)
    List<DailyWorkloadSnapshot> findHistoryForUser(
            @Param("userId") UUID userId,
            @Param("from")   LocalDate from
    );
}