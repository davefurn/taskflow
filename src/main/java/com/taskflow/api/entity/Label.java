package com.taskflow.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "labels",
        indexes = {
                @Index(name = "idx_labels_project", columnList = "project_id")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Label {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "project_id",
            foreignKey = @ForeignKey(name = "fk_labels_project")
    )
    private Project project;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    private String colour = "#EF4444";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}