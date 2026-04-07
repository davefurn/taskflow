package com.taskflow.api.repository.projects;

import com.taskflow.api.entity.ProjectMember;
import com.taskflow.api.entity.embeddable.ProjectMemberId;
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
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {

    // GET /api/projects/{id}/members
    @Query("""
        SELECT pm FROM ProjectMember pm
        JOIN FETCH pm.user
        WHERE pm.project.id = :projectId
    """)
    List<ProjectMember> findAllByProjectId(@Param("projectId") UUID projectId);

    // DELETE /api/projects/{id}/members/{userId}
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM ProjectMember pm
        WHERE pm.project.id = :projectId
        AND pm.user.id = :userId
    """)
    void deleteByProjectIdAndUserId(
            @Param("projectId") UUID projectId,
            @Param("userId") UUID userId
    );

    // Role check — used for access control on every project endpoint
    @Query("""
        SELECT pm FROM ProjectMember pm
        WHERE pm.project.id = :projectId
        AND pm.user.id = :userId
    """)
    Optional<ProjectMember> findByProjectIdAndUserId(
            @Param("projectId") UUID projectId,
            @Param("userId") UUID userId
    );

    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);

    long countByProjectId(UUID projectId);
}