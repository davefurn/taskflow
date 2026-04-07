package com.taskflow.api.repository.notifications;

import com.taskflow.api.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

    // GET /api/users/me — includes notificationPrefs
    // PUT /api/users/me/notifications
    Optional<NotificationPreference> findByUserId(UUID userId);
}