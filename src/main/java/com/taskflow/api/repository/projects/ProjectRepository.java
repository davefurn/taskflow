package com.taskflow.api.repository.projects;

import com.taskflow.api.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    // GET /api/projects?workspaceId=&status=&search=
    @Query("""
        SELECT p FROM Project p
        JOIN ProjectMember pm ON pm.project.id = p.id
        WHERE pm.user.id = :userId
        AND (:workspaceId IS NULL OR p.workspace.id = :workspaceId)
        AND (:status IS NULL OR p.status = :status)
        AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY p.createdAt DESC
    """)
    List<Project> findAllWithFilters(
            @Param("userId") UUID userId,
            @Param("workspaceId") UUID workspaceId,
            @Param("status") Project.Status status,
            @Param("search") String search
    );

    // GET /api/projects/{id} — full object with members and statuses
    @Query("""
        SELECT p FROM Project p
        LEFT JOIN FETCH p.workspace
        LEFT JOIN FETCH p.lead
        WHERE p.id = :id
    """)
    Optional<Project> findByIdWithDetails(@Param("id") UUID id);

    // POST /api/projects/{id}/archive
    // handled by findById + status update in service

    // Count projects per workspace — used in GET /api/workspaces
    long countByWorkspaceId(UUID workspaceId);

    // Used by analytics
    List<Project> findAllByWorkspaceId(UUID workspaceId);

    long countByStatus(Project.Status status);
}