package com.taskflow.api.security;

import com.taskflow.api.entity.User;
import com.taskflow.api.exception.UnauthorizedException;
import com.taskflow.api.repository.authAndUsers.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;

    // Call this from any service to get the currently logged-in User entity
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("Not authenticated");
        }

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        // username is stored as email in our UserDetailsService
        String email = userDetails.getUsername();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User session is invalid"));
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public boolean isAdmin() {
        return getCurrentUser().getRole() == User.Role.admin;
    }

    public boolean isManagerOrAbove() {
        User.Role role = getCurrentUser().getRole();
        return role == User.Role.admin || role == User.Role.manager;
    }
}