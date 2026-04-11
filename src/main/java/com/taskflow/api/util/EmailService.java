//package com.taskflow.api.util;
//
//import com.resend.Resend;
//import com.resend.core.exception.ResendException;
//import com.resend.services.emails.model.CreateEmailOptions;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//public class EmailService {
//
//    private final Resend resend;
//    private final String from;
//    private final String baseUrl;
//
//    public EmailService(
//            @Value("${app.resend.api-key}") String apiKey,
//            @Value("${app.mail.from}") String from,
//            @Value("${app.base-url}") String baseUrl) {
//        this.resend = new Resend(apiKey);
//        this.from = from;
//        this.baseUrl = baseUrl;
//    }
//
//    @Async
//    public void sendEmailVerification(String toEmail, String token) {
//        String link = baseUrl + "/verify-email?token=" + token;
//        send(toEmail,
//                "Verify your TaskFlow email",
//                "<h2>Welcome to TaskFlow</h2>" +
//                        "<p>Please verify your email by clicking the link below:</p>" +
//                        "<a href='" + link + "' style='background:#6366F1;color:white;padding:12px 24px;" +
//                        "border-radius:6px;text-decoration:none;display:inline-block'>Verify Email</a>" +
//                        "<p>This link expires in 24 hours.</p>"
//        );
//    }
//
//    @Async
//    public void sendInvitation(String toEmail, String name, String tempPassword) {
//        send(toEmail,
//                "You've been invited to TaskFlow",
//                "<h2>Hi " + name + ",</h2>" +
//                        "<p>You've been invited to TaskFlow.</p>" +
//                        "<p><b>Login:</b> <a href='" + baseUrl + "/login'>" + baseUrl + "/login</a></p>" +
//                        "<p><b>Temporary password:</b> <code>" + tempPassword + "</code></p>" +
//                        "<p>You will be asked to change your password on first login.</p>"
//        );
//    }
//
//    @Async
//    public void sendPasswordReset(String toEmail, String token) {
//        String link = baseUrl + "/reset-password?token=" + token;
//        send(toEmail,
//                "Reset your TaskFlow password",
//                "<h2>Password Reset</h2>" +
//                        "<p>Click the link below to reset your password:</p>" +
//                        "<a href='" + link + "' style='background:#6366F1;color:white;padding:12px 24px;" +
//                        "border-radius:6px;text-decoration:none;display:inline-block'>Reset Password</a>" +
//                        "<p>This link expires in 1 hour.</p>" +
//                        "<p>If you didn't request this, ignore this email.</p>"
//        );
//    }
//
//    @Async
//    public void sendAdminPasswordReset(String toEmail, String name, String tempPassword) {
//        send(toEmail,
//                "Your TaskFlow password has been reset",
//                "<h2>Hi " + name + ",</h2>" +
//                        "<p>An admin has reset your password.</p>" +
//                        "<p><b>Temporary password:</b> <code>" + tempPassword + "</code></p>" +
//                        "<p>Please log in and change it immediately.</p>"
//        );
//    }
//
//    @Async
//    public void sendTaskAssigned(String toEmail, String taskTitle, String projectName) {
//        send(toEmail,
//                "You've been assigned to a task",
//                "<h2>New Task Assignment</h2>" +
//                        "<p><b>Task:</b> " + taskTitle + "</p>" +
//                        "<p><b>Project:</b> " + projectName + "</p>" +
//                        "<a href='" + baseUrl + "'>View in TaskFlow</a>"
//        );
//    }
//
//    @Async
//    public void sendMentionNotification(String toEmail, String mentionedBy, String taskTitle) {
//        send(toEmail,
//                mentionedBy + " mentioned you in a comment",
//                "<h2>You were mentioned</h2>" +
//                        "<p><b>" + mentionedBy + "</b> mentioned you in: " + taskTitle + "</p>" +
//                        "<a href='" + baseUrl + "'>View in TaskFlow</a>"
//        );
//    }
//
//    @Async
//    public void sendDueTomorrowReminder(String toEmail, String taskTitle) {
//        send(toEmail,
//                "Task due tomorrow: " + taskTitle,
//                "<h2>Reminder</h2>" +
//                        "<p>This task is due tomorrow: <b>" + taskTitle + "</b></p>" +
//                        "<a href='" + baseUrl + "'>View in TaskFlow</a>"
//        );
//    }
//
//    @Async
//    public void sendOverdueNotification(String toEmail, String taskTitle) {
//        send(toEmail,
//                "Overdue task: " + taskTitle,
//                "<h2>Task Overdue</h2>" +
//                        "<p>This task is now overdue: <b>" + taskTitle + "</b></p>" +
//                        "<a href='" + baseUrl + "'>View in TaskFlow</a>"
//        );
//    }
//
//    private void send(String to, String subject, String html) {
//        try {
//            CreateEmailOptions params = CreateEmailOptions.builder()
//                    .from(from)
//                    .to(to)
//                    .subject(subject)
//                    .html(html)
//                    .build();
//
//            resend.emails().send(params);
//            log.info("Email sent to {} - subject: {}", to, subject);
//
//        } catch (ResendException e) {
//            // Never let email failure crash the main flow
//            log.error("Failed to send email to {}: {}", to, e.getMessage());
//        }
//    }
//}
//}

package com.taskflow.api.util;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final Resend resend;
    private final String from;
    private final String baseUrl;

    public EmailService(
            @Value("${app.resend.api-key}") String apiKey,
            @Value("${app.mail.from}") String from,
            @Value("${app.base-url}") String baseUrl) {
        this.resend  = new Resend(apiKey);
        this.from    = from;
        this.baseUrl = baseUrl;
    }

    @Async
    public void sendEmailVerification(String toEmail, String token) {
        String link = baseUrl + "/verify-email?token=" + token;
        send(toEmail,
                "Verify your TaskFlow email",
                "<h2>Welcome to TaskFlow</h2>" +
                        "<p>Please verify your email by clicking the link below:</p>" +
                        "<a href='" + link + "' style='background:#6366F1;color:white;" +
                        "padding:12px 24px;border-radius:6px;text-decoration:none;" +
                        "display:inline-block'>Verify Email</a>" +
                        "<p style='color:#6b7280;font-size:14px'>This link expires in 24 hours.</p>"
        );
    }

    @Async
    public void sendInvitation(String toEmail, String name, String tempPassword) {
        send(toEmail,
                "You've been invited to TaskFlow",
                "<h2>Hi " + name + ",</h2>" +
                        "<p>You've been invited to TaskFlow.</p>" +
                        "<p><b>Login:</b> <a href='" + baseUrl + "/login'>" + baseUrl + "/login</a></p>" +
                        "<p><b>Temporary password:</b> <code style='background:#f3f4f6;" +
                        "padding:4px 8px;border-radius:4px'>" + tempPassword + "</code></p>" +
                        "<p style='color:#6b7280;font-size:14px'>" +
                        "You will be asked to change your password on first login.</p>"
        );
    }

    @Async
    public void sendPasswordReset(String toEmail, String token) {
        String link = baseUrl + "/reset-password?token=" + token;
        send(toEmail,
                "Reset your TaskFlow password",
                "<h2>Password Reset</h2>" +
                        "<p>Click the link below to reset your password:</p>" +
                        "<a href='" + link + "' style='background:#6366F1;color:white;" +
                        "padding:12px 24px;border-radius:6px;text-decoration:none;" +
                        "display:inline-block'>Reset Password</a>" +
                        "<p style='color:#6b7280;font-size:14px'>This link expires in 1 hour.</p>" +
                        "<p style='color:#6b7280;font-size:14px'>" +
                        "If you didn't request this, ignore this email.</p>"
        );
    }

    @Async
    public void sendAdminPasswordReset(String toEmail, String name,
                                       String tempPassword) {
        send(toEmail,
                "Your TaskFlow password has been reset",
                "<h2>Hi " + name + ",</h2>" +
                        "<p>An admin has reset your password.</p>" +
                        "<p><b>Temporary password:</b> <code style='background:#f3f4f6;" +
                        "padding:4px 8px;border-radius:4px'>" + tempPassword + "</code></p>" +
                        "<p style='color:#6b7280;font-size:14px'>" +
                        "Please log in and change it immediately.</p>"
        );
    }

    @Async
    public void sendTaskAssigned(String toEmail, String taskTitle,
                                 String projectName) {
        send(toEmail,
                "You've been assigned to a task",
                "<h2>New Task Assignment</h2>" +
                        "<p><b>Task:</b> " + taskTitle + "</p>" +
                        "<p><b>Project:</b> " + projectName + "</p>" +
                        "<a href='" + baseUrl + "' style='color:#6366F1'>View in TaskFlow</a>"
        );
    }

    @Async
    public void sendMentionNotification(String toEmail, String mentionedBy,
                                        String taskTitle) {
        send(toEmail,
                mentionedBy + " mentioned you in a comment",
                "<h2>You were mentioned</h2>" +
                        "<p><b>" + mentionedBy + "</b> mentioned you in: <b>" + taskTitle + "</b></p>" +
                        "<a href='" + baseUrl + "' style='color:#6366F1'>View in TaskFlow</a>"
        );
    }

    @Async
    public void sendDueTomorrowReminder(String toEmail, String taskTitle) {
        send(toEmail,
                "Task due tomorrow: " + taskTitle,
                "<h2>Reminder</h2>" +
                        "<p>This task is due tomorrow: <b>" + taskTitle + "</b></p>" +
                        "<a href='" + baseUrl + "' style='color:#6366F1'>View in TaskFlow</a>"
        );
    }

    @Async
    public void sendOverdueNotification(String toEmail, String taskTitle) {
        send(toEmail,
                "Overdue task: " + taskTitle,
                "<h2>Task Overdue</h2>" +
                        "<p>This task is now overdue: <b>" + taskTitle + "</b></p>" +
                        "<a href='" + baseUrl + "' style='color:#6366F1'>View in TaskFlow</a>"
        );
    }

    // ── Private helper ────────────────────────────────────────

    private void send(String to, String subject, String html) {
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(from)
                    .to(to)
                    .subject(subject)
                    .html(html)
                    .build();

            resend.emails().send(params);
            log.info("Email sent to {} — subject: {}", to, subject);

        } catch (ResendException e) {
            // Never let email failure crash the main flow
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}