package com.taskflow.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "tasks",
        indexes = {
                @Index(name = "idx_tasks_project",  columnList = "project_id"),
                @Index(name = "idx_tasks_status",   columnList = "status_id"),
                @Index(name = "idx_tasks_parent",   columnList = "parent_task_id"),
                @Index(name = "idx_tasks_due_date", columnList = "due_date"),
                @Index(name = "idx_tasks_created",  columnList = "created_at"),
                @Index(name = "idx_tasks_group",    columnList = "task_group_id")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Task {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "project_id",
            foreignKey = @ForeignKey(name = "fk_tasks_project")
    )
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "task_group_id",
            foreignKey = @ForeignKey(name = "fk_tasks_group")
    )
    private TaskGroup taskGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "status_id",
            foreignKey = @ForeignKey(name = "fk_tasks_status")
    )
    private TaskStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "created_by",
            foreignKey = @ForeignKey(name = "fk_tasks_created_by")
    )
    private User createdBy;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "parent_task_id",
            foreignKey = @ForeignKey(name = "fk_tasks_parent")
    )
    private Task parentTask;

    @OneToMany(mappedBy = "parentTask", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Task> subtasks = new ArrayList<>();


    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskAssignee> assignees = new ArrayList<>();

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskLabel> labels = new ArrayList<>();

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskDependency> dependencies = new ArrayList<>();

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TimeEntry> timeEntries = new ArrayList<>();


    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Priority priority = Priority.none;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "estimated_hours", precision = 6, scale = 1)
    private BigDecimal estimatedHours;

    private Integer weight;

    @Builder.Default
    private Integer position = 0;

    @Column(name = "is_recurring")
    @Builder.Default
    private boolean isRecurring = false;

    @Column(name = "recurrence_rule")
    private String recurrenceRule;

    @Column(name = "completed_at")
    private Instant completedAt;



    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;



    public enum Priority {
        urgent, high, medium, low, none
    }
}