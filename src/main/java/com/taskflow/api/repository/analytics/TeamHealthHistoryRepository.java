package com.taskflow.api.repository.analytics;

import com.taskflow.api.entity.TeamHealthHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TeamHealthHistoryRepository extends JpaRepository<TeamHealthHistory, UUID> {

    // GET /api/analytics/team-health/history?workspaceId=&periods=12
    List<TeamHealthHistory> findAllByWorkspaceIdAndScoreDateAfterOrderByScoreDateAsc(
            UUID workspaceId,
            LocalDate after
    );

    // GET /api/analytics/team-health — latest score
    TeamHealthHistory findTopByWorkspaceIdOrderByScoreDateDesc(UUID workspaceId);
}