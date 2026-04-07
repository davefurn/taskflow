package com.taskflow.api.repository.taskStatusesAndGroups;

import com.taskflow.api.entity.TaskStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatus, UUID> {

    // GET /api/projects/{projectId}/statuses
    List<TaskStatus> findAllByProjectIdOrderByPositionAsc(UUID projectId);

    // PUT /api/projects/{projectId}/statuses/reorder
    // Handled by fetching all and updating positions in service

    // DELETE /api/projects/{projectId}/statuses/{id}?reassignTo=
    // Handled in service: reassign tasks then delete

    // Used when creating a project — get the default status
    Optional<TaskStatus> findByProjectIdAndIsDefaultTrue(UUID projectId);

    // Used by analytics — find done states
    List<TaskStatus> findAllByProjectIdAndIsDoneStateTrue(UUID projectId);

    // Max position — needed when adding a new status
    @Query("SELECT COALESCE(MAX(ts.position), 0) FROM TaskStatus ts WHERE ts.project.id = :projectId")
    int findMaxPositionByProjectId(@Param("projectId") UUID projectId);

    // Bulk position update for reorder
    @Modifying
    @Transactional
    @Query("UPDATE TaskStatus ts SET ts.position = :position WHERE ts.id = :id")
    void updatePosition(@Param("id") UUID id, @Param("position") int position);
}