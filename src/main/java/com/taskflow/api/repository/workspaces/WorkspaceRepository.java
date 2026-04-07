package com.taskflow.api.repository.workspaces;

import com.taskflow.api.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    // GET /api/workspaces — only workspaces the current user belongs to
    @Query("""
        SELECT w FROM Workspace w
        JOIN WorkspaceMember wm ON wm.workspace.id = w.id
        WHERE wm.user.id = :userId
        ORDER BY w.name ASC
    """)
    List<Workspace> findAllByUserId(@Param("userId") UUID userId);

    // Used to check workspace exists before adding members
    boolean existsById(UUID id);
}