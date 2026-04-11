package com.taskflow.api.controller;

import com.taskflow.api.dto.request.auth.*;
import com.taskflow.api.dto.response.ApiResponse;
import com.taskflow.api.dto.response.auth.AuthResponse;
import com.taskflow.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Login and receive JWT token")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        return authService.login(request, clientIp);
    }

    @PostMapping("/verify-email")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Verify email address using token from email link")
    public ApiResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ApiResponse.of("Email verified. You can now log in.");
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Change password for currently logged-in user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ApiResponse changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.of("Password changed successfully.");
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Request a password reset email")
    public ApiResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        // Always same message — never reveal if email exists
        return ApiResponse.of("If this email exists, a reset link has been sent.");
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Reset password using token from email link")
    public ApiResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.of("Password reset successfully.");
    }

    // Helper

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}