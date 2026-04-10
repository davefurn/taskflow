package com.taskflow.api.service;

import com.taskflow.api.dto.request.user.InviteUserRequest;
import com.taskflow.api.dto.request.user.UpdateNotificationPrefsRequest;
import com.taskflow.api.dto.request.user.UpdateRoleRequest;
import com.taskflow.api.dto.request.user.UpdateUserRequest;
import com.taskflow.api.dto.response.*;
import com.taskflow.api.dto.response.user.NotificationPrefsResponse;
import com.taskflow.api.dto.response.user.UserDetailResponse;
import com.taskflow.api.dto.response.user.UserResponse;
import com.taskflow.api.entity.*;
import com.taskflow.api.exception.*;
import com.taskflow.api.repository.authAndUsers.PasswordResetTokenRepository;
import com.taskflow.api.repository.authAndUsers.UserRepository;
import com.taskflow.api.repository.notifications.NotificationPreferenceRepository;
import com.taskflow.api.repository.workspaces.WorkspaceMemberRepository;
import com.taskflow.api.repository.workspaces.WorkspaceRepository;
import com.taskflow.api.security.SecurityUtil;
import com.taskflow.api.util.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtil securityUtil;
    private final EmailService emailService;

    // GET /api/users

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers(UUID workspaceId, User.Role role, String search) {

        // Only admins and managers can list users
        User current = securityUtil.getCurrentUser();
        if (current.getRole() != User.Role.admin && current.getRole() != User.Role.manager) {
            throw new ForbiddenException("You do not have permission to list users.");
        }

        return userRepository.findAllWithFilters(
                        workspaceId,
                        role != null ? role.name() : null,   // convert enum to string
                        search
                )
                .stream()
                .map(this::toUserResponse)
                .toList();
    }

    //POST /api/users/invite

    @Transactional
    public UserResponse inviteUser(InviteUserRequest request) {

        // Admin only
        User current = securityUtil.getCurrentUser();
        if (current.getRole() != User.Role.admin) {
            throw new ForbiddenException("Only admins can invite users.");
        }

        // Check email not already taken
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new ConflictException("A user with this email already exists.");
        }

        // Validate all workspaces exist
        for (UUID wsId : request.getWorkspaceIds()) {
            if (!workspaceRepository.existsById(wsId)) {
                throw new ResourceNotFoundException("Workspace not found with id: " + wsId);
            }
        }

        // Generate temporary password
        String tempPassword = generateTempPassword();

        // Create user — mustChangePwd = true so they're forced to change on first login
        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .role(request.getRole())
                .emailVerified(true) // invited users skip email verification
                .mustChangePwd(true)
                .invitedBy(current)
                .timezone("UTC")
                .build();

        userRepository.save(user);

        // Add to workspaces
        for (UUID wsId : request.getWorkspaceIds()) {
            Workspace workspace = workspaceRepository.findById(wsId)
                    .orElseThrow(() -> new ResourceNotFoundException("Workspace", wsId));
            WorkspaceMember member = WorkspaceMember.builder()
                    .id(new com.taskflow.api.entity.embeddable.WorkspaceMemberId(wsId, user.getId()))
                    .workspace(workspace)
                    .user(user)
                    .build();
            workspaceMemberRepository.save(member);
        }

        // Create default notification preferences
        notificationPreferenceRepository.save(
                NotificationPreference.builder()
                        .user(user)
                        .build()
        );

        // Send invitation email with temp password
        emailService.sendInvitation(user.getEmail(), user.getName(), tempPassword);

        log.info("User invited: {} by {}", user.getEmail(), current.getEmail());
        return toUserResponse(user);
    }

    // PUT /api/users/{id}

    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {

        User current = securityUtil.getCurrentUser();

        // Users can update themselves, admins can update anyone
        if (!current.getId().equals(id) && current.getRole() != User.Role.admin) {
            throw new ForbiddenException("You can only update your own profile.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (request.getName() != null)      user.setName(request.getName().trim());
        if (request.getJobTitle() != null)  user.setJobTitle(request.getJobTitle().trim());
        if (request.getTimezone() != null)  user.setTimezone(request.getTimezone());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());

        userRepository.save(user);
        log.info("User updated: {}", user.getEmail());
        return toUserResponse(user);
    }

    // PUT /api/users/{id}/role

    @Transactional
    public UserResponse updateRole(UUID id, UpdateRoleRequest request) {

        // Admin only
        User current = securityUtil.getCurrentUser();
        if (current.getRole() != User.Role.admin) {
            throw new ForbiddenException("Only admins can change user roles.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Prevent demoting the last admin
        if (user.getRole() == User.Role.admin
                && request.getRole() != User.Role.admin
                && !anotherAdminExists(id)) {
            throw new BadRequestException(
                    "Cannot change role - this is the last admin account."
            );
        }

        user.setRole(request.getRole());
        userRepository.save(user);
        log.info("Role updated for user {} to {}", user.getEmail(), request.getRole());
        return toUserResponse(user);
    }

    // DELETE /api/users/{id}

    @Transactional
    public void deleteUser(UUID id) {

        User current = securityUtil.getCurrentUser();
        if (current.getRole() != User.Role.admin) {
            throw new ForbiddenException("Only admins can delete users.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Prevent deleting the last admin
        if (user.getRole() == User.Role.admin && !anotherAdminExists(id)) {
            throw new BadRequestException(
                    "Cannot delete the last admin account."
            );
        }
        //implementing soft delete
        user.setActive(false);
        userRepository.save(user);

//        userRepository.delete(user);
        log.info("User deleted: {}", user.getEmail());
    }

    // POST /api/users/{id}/reset-password

    @Transactional
    public void adminResetPassword(UUID id) {

        User current = securityUtil.getCurrentUser();
        if (current.getRole() != User.Role.admin) {
            throw new ForbiddenException("Only admins can reset user passwords.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        String tempPassword = generateTempPassword();
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.setMustChangePwd(true);
        userRepository.save(user);

        emailService.sendAdminPasswordReset(user.getEmail(), user.getName(), tempPassword);
        log.info("Password reset by admin for user: {}", user.getEmail());
    }

    // GET /api/users/me

    @Transactional(readOnly = true)
    public UserDetailResponse getMe() {

        User current = securityUtil.getCurrentUser();

        List<WorkspaceMember> memberships =
                workspaceMemberRepository.findAllByUserId(current.getId());

        NotificationPreference prefs =
                notificationPreferenceRepository.findByUserId(current.getId())
                        .orElse(null);

        return UserDetailResponse.builder()
                .id(current.getId())
                .name(current.getName())
                .email(current.getEmail())
                .role(current.getRole())
                .jobTitle(current.getJobTitle())
                .avatarUrl(current.getAvatarUrl())
                .timezone(current.getTimezone())
                .lastLogin(current.getLastLogin())
                .workspaces(memberships.stream()
                        .map(wm -> UserDetailResponse.WorkspaceSummary.builder()
                                .id(wm.getWorkspace().getId())
                                .name(wm.getWorkspace().getName())
                                .description(wm.getWorkspace().getDescription())
                                .build())
                        .toList())
                .notificationPrefs(prefs != null ? toNotificationPrefsResponse(prefs) : null)
                .build();
    }

    //PUT /api/users/me/notifications

    @Transactional
    public NotificationPrefsResponse updateNotificationPrefs(
            UpdateNotificationPrefsRequest request) {

        User current = securityUtil.getCurrentUser();

        NotificationPreference prefs =
                notificationPreferenceRepository.findByUserId(current.getId())
                        .orElseGet(() -> NotificationPreference.builder()
                                .user(current)
                                .build());

        if (request.getTaskAssigned() != null)
            prefs.setTaskAssigned(request.getTaskAssigned());
        if (request.getMentionedInComment() != null)
            prefs.setMentionedInComment(request.getMentionedInComment());
        if (request.getTaskDueTomorrow() != null)
            prefs.setTaskDueTomorrow(request.getTaskDueTomorrow());
        if (request.getTaskOverdue() != null)
            prefs.setTaskOverdue(request.getTaskOverdue());
        if (request.getStatusChanges() != null)
            prefs.setStatusChanges(request.getStatusChanges());
        if (request.getWeeklySummary() != null)
            prefs.setWeeklySummary(request.getWeeklySummary());
        if (request.getEmailEnabled() != null)
            prefs.setEmailEnabled(request.getEmailEnabled());

        notificationPreferenceRepository.save(prefs);
        return toNotificationPrefsResponse(prefs);
    }

    //Helpers

    private boolean anotherAdminExists(UUID excludeUserId) {
        return userRepository.findAllWithFilters(null, User.Role.admin.name(), null)
                .stream()
                .anyMatch(u -> !u.getId().equals(excludeUserId));
    }

    private String generateTempPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .jobTitle(user.getJobTitle())
                .avatarUrl(user.getAvatarUrl())
                .lastLogin(user.getLastLogin())
                .build();
    }

    private NotificationPrefsResponse toNotificationPrefsResponse(
            NotificationPreference prefs) {
        return NotificationPrefsResponse.builder()
                .taskAssigned(prefs.isTaskAssigned())
                .mentionedInComment(prefs.isMentionedInComment())
                .taskDueTomorrow(prefs.isTaskDueTomorrow())
                .taskOverdue(prefs.isTaskOverdue())
                .statusChanges(prefs.isStatusChanges())
                .weeklySummary(prefs.isWeeklySummary())
                .emailEnabled(prefs.isEmailEnabled())
                .build();
    }
}