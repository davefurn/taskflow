package com.taskflow.api.service;

import com.taskflow.api.dto.request.auth.SetupRequest;
import com.taskflow.api.entity.CompanySetting;
import com.taskflow.api.entity.NotificationPreference;
import com.taskflow.api.entity.PasswordResetToken;
import com.taskflow.api.entity.User;
import com.taskflow.api.exception.ConflictException;
import com.taskflow.api.repository.companySettings.CompanySettingRepository;
import com.taskflow.api.repository.notifications.NotificationPreferenceRepository;
import com.taskflow.api.repository.authAndUsers.PasswordResetTokenRepository;
import com.taskflow.api.repository.authAndUsers.UserRepository;
import com.taskflow.api.util.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SetupService {

    private final UserRepository userRepository;
    private final CompanySettingRepository companySettingsRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public void initialSetup(SetupRequest request) {

        // Guard — only runs once
        if (companySettingsRepository.existsBy()) {
            throw new ConflictException(
                    "Setup already completed. An admin account already " +
                            "exists."
            );
        }

        // Validate email not taken (edge case: concurrent requests)
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new ConflictException("An account with this email already exists.");
        }

        // 1. Save company name
        companySettingsRepository.save(
                CompanySetting.builder()
                        .name(request.getCompanyName().trim())
                        .build()
        );

        // 2. Create admin user
        User admin = User.builder()
                .name(request.getFullName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.admin)
                .emailVerified(false)
                .mustChangePwd(false)
                .timezone("UTC")
                .build();

        userRepository.save(admin);

        // 3. Create default notification preferences for admin
        NotificationPreference prefs = NotificationPreference.builder()
                .user(admin)
                .taskAssigned(true)
                .mentionedInComment(true)
                .taskDueTomorrow(true)
                .taskOverdue(true)
                .statusChanges(false)
                .weeklySummary(true)
                .emailEnabled(true)
                .build();

        notificationPreferenceRepository.save(prefs);

        // 4. Generate email verification token (24h expiry)
        String token = UUID.randomUUID().toString();
        passwordResetTokenRepository.save(
                PasswordResetToken.builder()
                        .user(admin)
                        .token(token)
                        .type(PasswordResetToken.TokenType.email_verification)
                        .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                        .build()
        );

        // 5. Send verification email (async — never blocks response)
        emailService.sendEmailVerification(admin.getEmail(), token);

        log.info("Initial setup complete. Admin created: {}", admin.getEmail());
    }
}