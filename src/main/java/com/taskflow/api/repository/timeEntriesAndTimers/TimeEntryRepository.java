package com.taskflow.api.repository.timeEntriesAndTimers;

import com.taskflow.api.entity.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    // GET /api/tasks/{taskId}/time-entries
    List<TimeEntry> findAllByTaskIdOrderByDateDesc(UUID taskId);

    // GET /api/users/me/timesheet?startDate=&endDate=
    @Query("""
        SELECT te FROM TimeEntry te
        JOIN FETCH te.task t
        JOIN FETCH t.project
        WHERE te.user.id = :userId
        AND te.date >= :startDate
        AND te.date <= :endDate
        ORDER BY te.date ASC, te.createdAt ASC
    """)
    List<TimeEntry> findTimesheetEntries(
            @Param("userId")    UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate")   LocalDate endDate
    );

    // Total hours logged on a task — shown as "8.5h logged / 12h estimated"
    @Query("SELECT COALESCE(SUM(te.hours), 0) FROM TimeEntry te WHERE te.task.id = :taskId")
    java.math.BigDecimal sumHoursByTaskId(@Param("taskId") UUID taskId);

    // Analytics — hours logged by user in period
    @Query("""
        SELECT COALESCE(SUM(te.hours), 0) FROM TimeEntry te
        WHERE te.user.id = :userId
        AND te.date >= :startDate
        AND te.date <= :endDate
    """)
    java.math.BigDecimal sumHoursByUserAndPeriod(
            @Param("userId")    UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate")   LocalDate endDate
    );
}