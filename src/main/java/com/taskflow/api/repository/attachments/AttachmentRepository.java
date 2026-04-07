package com.taskflow.api.repository.attachments;

import com.taskflow.api.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    // GET /api/tasks/{taskId}/attachments
    List<Attachment> findAllByTaskIdOrderByCreatedAtDesc(UUID taskId);
}