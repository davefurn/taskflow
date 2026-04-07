package com.taskflow.api.repository.authAndUsers;

import com.taskflow.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Auth
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Setup check — is there any admin yet?
    boolean existsByRole(User.Role role);

    // GET /api/users?workspaceId=&role=&search=
    @Query("""
        SELECT DISTINCT u FROM User u
        JOIN WorkspaceMember wm ON wm.user = u
        WHERE (:workspaceId IS NULL OR wm.workspace.id = :workspaceId)
        AND   (:role        IS NULL OR u.role           = :role)
        AND   (
                :search IS NULL
                OR LOWER(u.name)  LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
              )
    """)
    List<User> findAllWithFilters(
            @Param("workspaceId") UUID workspaceId,
            @Param("role")        User.Role role,
            @Param("search")      String search
    );

    // GET /api/users/me
    // Simple findById is enough — workspaces loaded separately via WorkspaceMemberRepository
    // Keeping this as a plain findById avoids the JOIN FETCH owner problem entirely
    Optional<User> findById(UUID id);
}