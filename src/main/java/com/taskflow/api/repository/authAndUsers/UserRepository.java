package com.taskflow.api.repository.authAndUsers;

import com.taskflow.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
//    @Query(value = """
//    SELECT DISTINCT u.* FROM users u
//    LEFT JOIN workspace_members wm ON wm.user_id = u.id
//    WHERE (:workspaceId IS NULL OR wm.workspace_id = CAST(:workspaceId AS uuid))
//    AND   (:role IS NULL OR u.role = :role)
//    AND   (:search IS NULL
//           OR LOWER(u.name::text)  LIKE LOWER(CONCAT('%', :search, '%'))
//           OR LOWER(u.email::text) LIKE LOWER(CONCAT('%', :search, '%')))
//""", nativeQuery = true)
//    List<User> findAllWithFilters(
//            @Param("workspaceId") UUID workspaceId,
//            @Param("role") String role,
//            @Param("search") String search
//    );
    // GET /api/users?workspaceId=&role=&search=
    @Query(value = """
    SELECT DISTINCT u.* FROM users u
    LEFT JOIN workspace_members wm ON wm.user_id = u.id
    WHERE u.is_active = true 
    AND   (:workspaceId IS NULL OR wm.workspace_id = CAST(:workspaceId AS uuid))
    AND   (:role IS NULL OR u.role = :role)
    AND   (:search IS NULL
           OR LOWER(u.name::text)  LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(u.email::text) LIKE LOWER(CONCAT('%', :search, '%')))
""", nativeQuery = true)
    List<User> findAllWithFilters(
            @Param("workspaceId") UUID workspaceId,
            @Param("role") String role,
            @Param("search") String search
    );

    // GET /api/users/me
    // Simple findById is enough — workspaces loaded separately via WorkspaceMemberRepository
    // Keeping this as a plain findById avoids the JOIN FETCH owner problem entirely
    Optional<User> findById(UUID id);


    // --- ESCAPE HATCHES ---
    // Use nativeQuery = true to bypass the @SQLRestriction

    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<User> findAllIncludingDeactivated();

    @Query(value = "SELECT * FROM users WHERE id = :id", nativeQuery = true)
    Optional<User> findByIdIncludingDeactivated(@Param("id") UUID id);

    @Modifying
    @Query(value = "UPDATE users SET is_active = true WHERE id = :id", nativeQuery = true)
    void reactivateUser(@Param("id") UUID id);
}