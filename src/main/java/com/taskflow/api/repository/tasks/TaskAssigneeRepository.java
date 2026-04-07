package com.taskflow.api.repository.tasks;

import com.taskflow.api.entity.TaskAssignee;
import com.taskflow.api.entity.embeddable.TaskAssigneeId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TaskAssigneeRepository extends JpaRepository<TaskAssignee, TaskAssigneeId> {
    @Modifying
    @Transactional
    @Query("DELETE FROM TaskAssignee ta WHERE ta.task.id = :taskId")
    void deleteAllByTaskId(@Param("taskId") UUID taskId);
}