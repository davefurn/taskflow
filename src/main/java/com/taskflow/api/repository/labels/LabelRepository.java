package com.taskflow.api.repository.labels;

import com.taskflow.api.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LabelRepository extends JpaRepository<Label, UUID> {

    // GET /api/projects/{projectId}/labels
    List<Label> findAllByProjectIdOrderByNameAsc(UUID projectId);

    // Validate label belongs to project before assigning
    boolean existsByIdAndProjectId(UUID id, UUID projectId);
}