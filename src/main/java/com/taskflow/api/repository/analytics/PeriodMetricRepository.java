package com.taskflow.api.repository.analytics;

import com.taskflow.api.entity.PeriodMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PeriodMetricRepository extends JpaRepository<PeriodMetric, UUID> {

    // GET /api/analytics/projects/{projectId}/velocity?periodType=week&periods=12
    List<PeriodMetric> findAllByProjectIdAndPeriodTypeOrderByPeriodStartAsc(
            UUID projectId,
            PeriodMetric.PeriodType periodType
    );

    // GET /api/analytics/projects/{projectId}/cycle-time
    List<PeriodMetric> findAllByProjectIdAndPeriodTypeAndPeriodStartAfterOrderByPeriodStartAsc(
            UUID projectId,
            PeriodMetric.PeriodType periodType,
            LocalDate after
    );
}