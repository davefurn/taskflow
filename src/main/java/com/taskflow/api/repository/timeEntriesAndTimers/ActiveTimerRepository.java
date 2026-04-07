package com.taskflow.api.repository.timeEntriesAndTimers;

import com.taskflow.api.entity.ActiveTimer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActiveTimerRepository extends JpaRepository<ActiveTimer, UUID> {

    // POST /api/tasks/{taskId}/timer/start — check user doesn't already have one running
    Optional<ActiveTimer> findByUserId(UUID userId);

    // POST /api/tasks/{taskId}/timer/stop
    Optional<ActiveTimer> findByUserIdAndTaskId(UUID userId, UUID taskId);
}