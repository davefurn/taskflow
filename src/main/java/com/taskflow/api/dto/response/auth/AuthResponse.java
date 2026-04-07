package com.taskflow.api.dto.response.auth;

import com.taskflow.api.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private long expiresIn;
    private UserSummary user;

    @Data
    @Builder
    @AllArgsConstructor
    public static class UserSummary {
        private UUID id;
        private String name;
        private String email;
        private User.Role role;
        private boolean mustChangePwd;
    }
}