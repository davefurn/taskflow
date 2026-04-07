package com.taskflow.api.controller;


import com.taskflow.api.dto.request.user.InviteUserRequest;
import com.taskflow.api.dto.request.user.UpdateNotificationPrefsRequest;
import com.taskflow.api.dto.request.user.UpdateRoleRequest;
import com.taskflow.api.dto.request.user.UpdateUserRequest;
import com.taskflow.api.dto.response.*;
import com.taskflow.api.dto.response.user.NotificationPrefsResponse;
import com.taskflow.api.dto.response.user.UserDetailResponse;
import com.taskflow.api.dto.response.user.UserResponse;
import com.taskflow.api.entity.User;
import com.taskflow.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "List users — filterable by workspace, role, search")
    public List<UserResponse> getUsers(
            @RequestParam(required = false) UUID workspaceId,
            @RequestParam(required = false) User.Role role,
            @RequestParam(required = false) String search) {
        return userService.getUsers(workspaceId, role, search);
    }

    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Invite a new user — admin only")
    public UserResponse inviteUser(@Valid @RequestBody InviteUserRequest request) {
        return userService.inviteUser(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current logged-in user with workspaces and notification prefs")
    public UserDetailResponse getMe() {
        return userService.getMe();
    }

    @PutMapping("/me/notifications")
    @Operation(summary = "Update notification preferences for current user")
    public NotificationPrefsResponse updateNotifications(
            @Valid @RequestBody UpdateNotificationPrefsRequest request) {
        return userService.updateNotificationPrefs(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user profile — admins can update anyone, users update themselves")
    public UserResponse updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Change user role — admin only")
    public UserResponse updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoleRequest request) {
        return userService.updateRole(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user — admin only")
    public void deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "Admin resets a user's password and sends them a temporary one")
    public ApiResponse resetPassword(@PathVariable UUID id) {
        userService.adminResetPassword(id);
        return ApiResponse.of("Temporary password sent to user's email.");
    }
}