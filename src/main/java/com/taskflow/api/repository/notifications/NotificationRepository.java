package com.taskflow.api.repository.notifications;

import com.taskflow.api.entity.Notification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // GET /api/notifications?unreadOnly=true&page=&size=
    @Query("""
        SELECT n FROM Notification n
        WHERE n.user.id = :userId
        AND (:unreadOnly = FALSE OR n.isRead = FALSE)
        ORDER BY n.createdAt DESC
    """)
    Page<Notification> findAllByUserId(
            @Param("userId")     UUID userId,
            @Param("unreadOnly") boolean unreadOnly,
            Pageable pageable
    );

    // Unread count — shown on bell icon badge
    long countByUserIdAndIsReadFalse(UUID userId);

    // PATCH /api/notifications/{id}/read
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = TRUE WHERE n.id = :id AND n.user.id = :userId")
    void markAsRead(@Param("id") UUID id, @Param("userId") UUID userId);

    // POST /api/notifications/read-all
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = TRUE WHERE n.user.id = :userId")
    void markAllAsRead(@Param("userId") UUID userId);
}