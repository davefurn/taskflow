package com.taskflow.api.repository.taskStatusesAndGroups;


import com.taskflow.api.entity.TaskGroup;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskGroupRepository extends JpaRepository<TaskGroup, UUID> {

    // GET /api/projects/{projectId}/groups
    List<TaskGroup> findAllByProjectIdOrderByPositionAsc(UUID projectId);

    // Max position — needed when adding a new group
    @Query("SELECT COALESCE(MAX(tg.position), 0) FROM TaskGroup tg WHERE tg.project.id = :projectId")
    int findMaxPositionByProjectId(@Param("projectId") UUID projectId);

    // PUT /api/projects/{projectId}/groups/reorder
    @Modifying
    @Transactional
    @Query("UPDATE TaskGroup tg SET tg.position = :position WHERE tg.id = :id")
    void updatePosition(@Param("id") UUID id, @Param("position") int position);

    // Task count per group — used in GET /api/projects/{projectId}/groups response
    @Query("SELECT COUNT(t) FROM Task t WHERE t.taskGroup.id = :groupId")
    long countTasksByGroupId(@Param("groupId") UUID groupId);


}