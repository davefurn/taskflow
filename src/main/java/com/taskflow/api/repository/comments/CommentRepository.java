package com.taskflow.api.repository.comments;

import com.taskflow.api.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // GET /api/tasks/{taskId}/comments — top-level only, replies nested
    @Query("""
        SELECT c FROM Comment c
        JOIN FETCH c.user
        WHERE c.task.id = :taskId
        AND c.parentComment IS NULL
        ORDER BY c.createdAt ASC
    """)
    List<Comment> findTopLevelByTaskId(@Param("taskId") UUID taskId);

    // Replies — loaded per parent comment
    @Query("""
        SELECT c FROM Comment c
        JOIN FETCH c.user
        WHERE c.parentComment.id = :parentId
        ORDER BY c.createdAt ASC
    """)
    List<Comment> findRepliesByParentId(@Param("parentId") UUID parentId);

    // Count comments — shown on task card in list/board view
    long countByTaskId(UUID taskId);
}