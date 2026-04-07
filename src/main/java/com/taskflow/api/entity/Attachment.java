package com.taskflow.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "attachments",
        indexes = {
                @Index(name = "idx_attachments_task", columnList = "task_id"),
                @Index(name = "idx_attachments_user", columnList = "uploaded_by")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Attachment {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "task_id",
            foreignKey = @ForeignKey(name = "fk_attachments_task")
    )
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "uploaded_by",
            foreignKey = @ForeignKey(name = "fk_attachments_uploaded_by")
    )
    private User uploadedBy;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type")
    private String mimeType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}