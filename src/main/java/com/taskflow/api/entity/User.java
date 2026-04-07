package com.taskflow.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(nullable = false)
    @Builder.Default
    private String timezone = "UTC";

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "must_change_pwd", nullable = false)
    @Builder.Default
    private boolean mustChangePwd = false;

    @Column(name = "weekly_capacity_hours", precision = 4, scale = 1)
    @Builder.Default
    private BigDecimal weeklyCapacityHours = new BigDecimal("40.0");

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "invited_by",
            foreignKey = @ForeignKey(name = "fk_users_invited_by")
    )
    private User invitedBy;

    @Column(name = "last_login")
    private Instant lastLogin;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public enum Role {
        admin, manager, member, viewer
    }
}