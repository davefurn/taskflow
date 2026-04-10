package com.taskflow.api.repository.workspaces;

import com.taskflow.api.entity.WorkspaceMember;
import com.taskflow.api.entity.embeddable.WorkspaceMemberId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, WorkspaceMemberId> {

    // GET /api/workspaces/{id}/members
    @Query("""
        SELECT wm FROM WorkspaceMember wm
        JOIN FETCH wm.user
        WHERE wm.workspace.id = :workspaceId
    """)
    List<WorkspaceMember> findAllByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    // DELETE /api/workspaces/{id}/members/{userId}
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM WorkspaceMember wm
        WHERE wm.workspace.id = :workspaceId
        AND wm.user.id = :userId
    """)
    void deleteByWorkspaceIdAndUserId(
            @Param("workspaceId") UUID workspaceId,
            @Param("userId") UUID userId
    );

    // Check membership before adding to project
    boolean existsByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);

    // Count members — used in GET /api/workspaces response
    long countByWorkspaceId(UUID workspaceId);
    // GET /api/users/me — load workspaces for current user
    @Query("""
    SELECT wm FROM WorkspaceMember wm
    JOIN FETCH wm.workspace
    WHERE wm.user.id = :userId
""")
    List<WorkspaceMember> findAllByUserId(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId")
    void deleteAllByWorkspaceId(@Param("workspaceId") UUID workspaceId);
}