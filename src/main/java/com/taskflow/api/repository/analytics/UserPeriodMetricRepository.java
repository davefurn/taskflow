package com.taskflow.api.repository.analytics;

import com.taskflow.api.entity.PeriodMetric;
import com.taskflow.api.entity.UserPeriodMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserPeriodMetricRepository extends JpaRepository<UserPeriodMetric, UUID> {

    // GET /api/analytics/users/{userId}/performance
    List<UserPeriodMetric> findAllByUserIdAndPeriodTypeAndPeriodStartAfterOrderByPeriodStartAsc(
            UUID userId,
            PeriodMetric.PeriodType periodType,
            LocalDate after
    );

    // Filtered by project too
    List<UserPeriodMetric> findAllByUserIdAndProjectIdAndPeriodTypeOrderByPeriodStartAsc(
            UUID userId,
            UUID projectId,
            PeriodMetric.PeriodType periodType
    );
}