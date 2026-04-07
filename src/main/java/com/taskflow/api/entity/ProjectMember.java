package com.taskflow.api.entity;

import com.taskflow.api.entity.embeddable.ProjectMemberId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "project_members",
        indexes = {
                @Index(name = "idx_project_members_user", columnList = "user_id")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProjectMember {

    @EmbeddedId
    private ProjectMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    @JoinColumn(
            name = "project_id",
            foreignKey = @ForeignKey(name = "fk_project_members_project")
    )
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "fk_project_members_user")
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.member;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private Instant joinedAt;

    public enum Role {
        lead, member, viewer
    }
}