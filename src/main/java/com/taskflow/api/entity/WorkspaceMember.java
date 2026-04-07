package com.taskflow.api.entity;

import com.taskflow.api.entity.embeddable.WorkspaceMemberId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "workspace_members",
        indexes = {
                @Index(name = "idx_workspace_members_user", columnList = "user_id")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WorkspaceMember {

    @EmbeddedId
    private WorkspaceMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("workspaceId")
    @JoinColumn(
            name = "workspace_id",
            foreignKey = @ForeignKey(name = "fk_workspace_members_workspace")
    )
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "fk_workspace_members_user")
    )
    private User user;

    @Column(name = "role_override")
    private String roleOverride;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private Instant joinedAt;
}