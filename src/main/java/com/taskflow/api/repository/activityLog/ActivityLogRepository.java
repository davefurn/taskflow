package com.taskflow.api.repository.activityLog;

import com.taskflow.api.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    // GET /api/tasks/{taskId}/activity
    List<ActivityLog> findAllByTaskIdOrderByCreatedAtAsc(UUID taskId);

    // GET /api/projects/{projectId}/activity?page=&size=
    Page<ActivityLog> findAllByProjectIdOrderByCreatedAtDesc(UUID projectId, Pageable pageable);
}