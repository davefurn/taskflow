package com.taskflow.api.service;

import com.taskflow.api.dto.request.auth.*;
import com.taskflow.api.dto.response.auth.AuthResponse;
import com.taskflow.api.entity.PasswordResetToken;
import com.taskflow.api.exception.*;
import com.taskflow.api.security.JwtUtil;
import com.taskflow.api.security.SecurityUtil;
import com.taskflow.api.util.EmailService;
import com.taskflow.api.util.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;


import com.taskflow.api.entity.User;
import com.taskflow.api.repository.notifications.NotificationPreferenceRepository;
import com.taskflow.api.repository.authAndUsers.PasswordResetTokenRepository;
import com.taskflow.api.repository.authAndUsers.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SecurityUtil securityUtil;
    private final EmailService emailService;
    private final LoginAttemptService loginAttemptService;

    //POST /api/auth/login

    @Transactional
    public AuthResponse login(LoginRequest request, String clientIp) {

        // Check IP not locked
        if (loginAttemptService.isBlocked(clientIp)) {
            throw new UnauthorizedException(
                    "Too many failed attempts. Please try again in 15 minutes."
            );
        }

        // Find user — always vague on error (don't reveal which field is wrong)
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> {
                    loginAttemptService.loginFailed(clientIp);
                    return new UnauthorizedException("Invalid email or password");
                });

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            loginAttemptService.loginFailed(clientIp);
            throw new UnauthorizedException("Invalid email or password");
        }

        // Check email verified
        if (!user.isEmailVerified()) {
            throw new UnauthorizedException(
                    "Please verify your email before logging in."
            );
        }

        // Success — reset attempt counter, update last login
        loginAttemptService.loginSucceeded(clientIp);
        user.setLastLogin(Instant.now());
        userRepository.save(user);

//        String token = jwtUtil.generateToken(
//                user.getId(), user.getEmail(), user.getRole().name()
//        );
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getRole().ordinal()   // 0=admin, 1=manager, 2=member, 3=viewer
        );

        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .expiresIn(jwtUtil.getExpirationMs())
                .user(AuthResponse.UserSummary.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .mustChangePwd(user.isMustChangePwd())
                        .build())
                .build();
    }

    //POST /api/auth/verify-email

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {

        PasswordResetToken tokenEntity = tokenRepository
                .findByTokenAndType(request.getToken(), PasswordResetToken.TokenType.email_verification)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Verification link is invalid or has already been used."
                ));

        // Check not expired
        if (tokenEntity.getExpiresAt().isBefore(Instant.now())) {
            throw new ValidationException(
                    "Verification link has expired. Please request a new one."
            );
        }

        // Mark user as verified
        User user = tokenEntity.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        // Delete token — one use only
        tokenRepository.deleteAllByUserIdAndType(
                user.getId(),
                PasswordResetToken.TokenType.email_verification
        );

        log.info("Email verified for user: {}", user.getEmail());
    }

    // POST /api/auth/change-password

    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        User user = securityUtil.getCurrentUser();

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect.");
        }

        // Prevent reusing the same password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException(
                    "New password must be different from your current password."
            );
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePwd(false);
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getEmail());
    }

    // POST /api/auth/forgot-password

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        // Always return 200 — never reveal if email exists
        userRepository.findByEmail(request.getEmail().toLowerCase())
                .ifPresent(user -> {

                    // Delete any existing reset tokens for this user
                    tokenRepository.deleteAllByUserIdAndType(
                            user.getId(),
                            PasswordResetToken.TokenType.password_reset
                    );

                    // Generate new token (1 hour expiry)
                    String token = UUID.randomUUID().toString();
                    tokenRepository.save(
                            PasswordResetToken.builder()
                                    .user(user)
                                    .token(token)
                                    .type(PasswordResetToken.TokenType.password_reset)
                                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                                    .build()
                    );

                    emailService.sendPasswordReset(user.getEmail(), token);
                    log.info("Password reset email sent to: {}", user.getEmail());
                });
    }

    // ── POST /api/auth/reset-password ────────────────────────

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetToken tokenEntity = tokenRepository
                .findByTokenAndTypeAndExpiresAtAfter(
                        request.getToken(),
                        PasswordResetToken.TokenType.password_reset,
                        Instant.now()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reset link is invalid or has expired."
                ));

        User user = tokenEntity.getUser();

        // Prevent reusing the same password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException(
                    "New password must be different from your previous password."
            );
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePwd(false);
        userRepository.save(user);

        // Delete all reset tokens for this user
        tokenRepository.deleteAllByUserIdAndType(
                user.getId(),
                PasswordResetToken.TokenType.password_reset
        );

        log.info("Password reset complete for user: {}", user.getEmail());
    }
}
//
//package com.taskflow.api.service;
//
//import com.taskflow.api.dto.request.auth.*;
//import com.taskflow.api.dto.response.auth.AuthResponse;
//import com.taskflow.api.entity.PasswordResetToken;
//import com.taskflow.api.exception.*;
//import com.taskflow.api.security.JwtUtil;
//import com.taskflow.api.security.SecurityUtil;
//import com.taskflow.api.util.EmailService;
//import com.taskflow.api.util.LoginAttemptService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Instant;
//import java.time.temporal.ChronoUnit;
//import java.util.UUID;
//
//import com.taskflow.api.entity.User;
//import com.taskflow.api.repository.notifications.NotificationPreferenceRepository;
//import com.taskflow.api.repository.authAndUsers.PasswordResetTokenRepository;
//import com.taskflow.api.repository.authAndUsers.UserRepository;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//
//    private final UserRepository userRepository;
//    private final PasswordResetTokenRepository tokenRepository;
//    private final NotificationPreferenceRepository notificationPreferenceRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtUtil jwtUtil;
//    private final SecurityUtil securityUtil;
//    private final EmailService emailService;
//    private final LoginAttemptService loginAttemptService;
//
//    //POST /api/auth/login
//
//    @Transactional
//    public AuthResponse login(LoginRequest request, String clientIp) {
//
//        // Check IP not locked
//        if (loginAttemptService.isBlocked(clientIp)) {
//            throw new UnauthorizedException(
//                    "Too many failed attempts. Please try again in 15 minutes."
//            );
//        }
//
//        // Find user — always vague on error (don't reveal which field is wrong)
//        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
//                .orElseThrow(() -> {
//                    loginAttemptService.loginFailed(clientIp);
//                    return new UnauthorizedException("Invalid email or password");
//                });
//
//        // SOFT DELETE CHECK: Block deactivated users
//        if (!user.isActive()) {
//            loginAttemptService.loginFailed(clientIp);
//            throw new UnauthorizedException("Your account has been deactivated. Please contact support.");
//        }
//
//        // Check password
//        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
//            loginAttemptService.loginFailed(clientIp);
//            throw new UnauthorizedException("Invalid email or password");
//        }
//
//        // Check email verified
//        if (!user.isEmailVerified()) {
//            throw new UnauthorizedException(
//                    "Please verify your email before logging in."
//            );
//        }
//
//        // Success — reset attempt counter, update last login
//        loginAttemptService.loginSucceeded(clientIp);
//        user.setLastLogin(Instant.now());
//        userRepository.save(user);
//
//        String token = jwtUtil.generateToken(
//                user.getId(), user.getEmail(), user.getRole().name()
//        );
//
//        log.info("User logged in: {}", user.getEmail());
//
//        return AuthResponse.builder()
//                .token(token)
//                .expiresIn(jwtUtil.getExpirationMs())
//                .user(AuthResponse.UserSummary.builder()
//                        .id(user.getId())
//                        .name(user.getName())
//                        .email(user.getEmail())
//                        .role(user.getRole())
//                        .mustChangePwd(user.isMustChangePwd())
//                        .build())
//                .build();
//    }
//
//    //POST /api/auth/verify-email
//
//    @Transactional
//    public void verifyEmail(VerifyEmailRequest request) {
//
//        PasswordResetToken tokenEntity = tokenRepository
//                .findByTokenAndType(request.getToken(), PasswordResetToken.TokenType.email_verification)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "Verification link is invalid or has already been used."
//                ));
//
//        // Check not expired
//        if (tokenEntity.getExpiresAt().isBefore(Instant.now())) {
//            throw new ValidationException(
//                    "Verification link has expired. Please request a new one."
//            );
//        }
//
//        User user = tokenEntity.getUser();
//
//        // SOFT DELETE CHECK: Don't let deactivated users verify emails
//        if (!user.isActive()) {
//            throw new BadRequestException("This account has been deactivated.");
//        }
//
//        // Mark user as verified
//        user.setEmailVerified(true);
//        userRepository.save(user);
//
//        // Delete token — one use only
//        tokenRepository.deleteAllByUserIdAndType(
//                user.getId(),
//                PasswordResetToken.TokenType.email_verification
//        );
//
//        log.info("Email verified for user: {}", user.getEmail());
//    }
//
//    // POST /api/auth/change-password
//
//    @Transactional
//    public void changePassword(ChangePasswordRequest request) {
//
//        User user = securityUtil.getCurrentUser();
//
//        // SOFT DELETE CHECK: Prevent mid-session changes if deactivated
//        if (!user.isActive()) {
//            throw new UnauthorizedException("Your account has been deactivated.");
//        }
//
//        // Verify current password
//        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
//            throw new UnauthorizedException("Current password is incorrect.");
//        }
//
//        // Prevent reusing the same password
//        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
//            throw new BadRequestException(
//                    "New password must be different from your current password."
//            );
//        }
//
//        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
//        user.setMustChangePwd(false);
//        userRepository.save(user);
//
//        log.info("Password changed for user: {}", user.getEmail());
//    }
//
//    // POST /api/auth/forgot-password
//
//    @Transactional
//    public void forgotPassword(ForgotPasswordRequest request) {
//
//        // Always return 200 — never reveal if email exists
//        userRepository.findByEmail(request.getEmail().toLowerCase())
//                .filter(User::isActive) // SOFT DELETE CHECK: Silently ignore inactive users
//                .ifPresent(user -> {
//
//                    // Delete any existing reset tokens for this user
//                    tokenRepository.deleteAllByUserIdAndType(
//                            user.getId(),
//                            PasswordResetToken.TokenType.password_reset
//                    );
//
//                    // Generate new token (1 hour expiry)
//                    String token = UUID.randomUUID().toString();
//                    tokenRepository.save(
//                            PasswordResetToken.builder()
//                                    .user(user)
//                                    .token(token)
//                                    .type(PasswordResetToken.TokenType.password_reset)
//                                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
//                                    .build()
//                    );
//
//                    emailService.sendPasswordReset(user.getEmail(), token);
//                    log.info("Password reset email sent to: {}", user.getEmail());
//                });
//    }
//
//    // ── POST /api/auth/reset-password ────────────────────────
//
//    @Transactional
//    public void resetPassword(ResetPasswordRequest request) {
//
//        PasswordResetToken tokenEntity = tokenRepository
//                .findByTokenAndTypeAndExpiresAtAfter(
//                        request.getToken(),
//                        PasswordResetToken.TokenType.password_reset,
//                        Instant.now()
//                )
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "Reset link is invalid or has expired."
//                ));
//
//        User user = tokenEntity.getUser();
//
//        // SOFT DELETE CHECK: Don't let deactivated users reset passwords
//        if (!user.isActive()) {
//            throw new BadRequestException("This account has been deactivated.");
//        }
//
//        // Prevent reusing the same password
//        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
//            throw new BadRequestException(
//                    "New password must be different from your previous password."
//            );
//        }
//
//        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
//        user.setMustChangePwd(false);
//        userRepository.save(user);
//
//        // Delete all reset tokens for this user
//        tokenRepository.deleteAllByUserIdAndType(
//                user.getId(),
//                PasswordResetToken.TokenType.password_reset
//        );
//
//        log.info("Password reset complete for user: {}", user.getEmail());
//    }
//}