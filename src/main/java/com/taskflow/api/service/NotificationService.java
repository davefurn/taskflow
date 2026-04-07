package com.taskflow.api.service;

import com.taskflow.api.dto.response.*;
import com.taskflow.api.dto.response.notification.NotificationResponse;
import com.taskflow.api.repository.notifications.NotificationRepository;
import com.taskflow.api.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SecurityUtil securityUtil;

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(boolean unreadOnly,
                                                               int page, int size) {
        UUID userId = securityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page - 1, size);

        var result = notificationRepository.findAllByUserId(userId, unreadOnly, pageable);
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);

        return PageResponse.<NotificationResponse>builder()
                .content(result.getContent().stream()
                        .map(n -> NotificationResponse.builder()
                                .id(n.getId())
                                .type(n.getType())
                                .title(n.getTitle())
                                .message(n.getMessage())
                                .linkUrl(n.getLinkUrl())
                                .isRead(n.isRead())
                                .createdAt(n.getCreatedAt())
                                .build())
                        .toList())
                .totalPages(result.getTotalPages())
                .totalElements(unreadCount)
                .build();
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        UUID userId = securityUtil.getCurrentUserId();
        notificationRepository.markAsRead(notificationId, userId);
    }

    @Transactional
    public void markAllAsRead() {
        UUID userId = securityUtil.getCurrentUserId();
        notificationRepository.markAllAsRead(userId);
    }
}