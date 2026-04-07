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

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal().equals("anonymousUser")) {
            throw new UnauthorizedException("Not authenticated");
        }

        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        // Username in UserDetails is stored as the user's UUID (set in UserDetailsServiceImpl)
        String username = userDetails.getUsername();

        try {
            UUID userId = UUID.fromString(username);
            return userRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("User session is invalid"));
        } catch (IllegalArgumentException e) {
            // Fallback — try as email
            return userRepository.findByEmail(username)
                    .orElseThrow(() -> new UnauthorizedException("User session is invalid"));
        }
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