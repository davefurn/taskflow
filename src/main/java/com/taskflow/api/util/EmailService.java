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
        String link = baseUrl + "/auth/verify-email?token=" + token;
        String content = """
                <p>Welcome to TaskFlow.</p>
                <p>To complete your registration and secure your account, please verify your email address by clicking the button below.</p>
                <p style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="background-color: #0f172a; color: #ffffff; padding: 12px 28px; border-radius: 6px; text-decoration: none; font-weight: 600; display: inline-block;">Verify Email Address</a>
                </p>
                <p style="color: #6b7280; font-size: 14px; text-align: center;">This verification link will expire in 24 hours.</p>
                """.formatted(link);

        send(toEmail, "Verify your TaskFlow Account", buildEmailTemplate("Account Verification", content));
    }

    @Async
    public void sendInvitation(String toEmail, String name, String tempPassword) {

        String loginUrl = baseUrl + "/auth/login";
        String content = """
                <p>Hello %s,</p>
                <p>You have been invited to join the TaskFlow platform.</p>
                <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; padding: 16px; border-radius: 6px; margin: 20px 0;">
                    <p style="margin: 0 0 10px 0;"><strong>Login URL:</strong> <a href="%s" style="color: #2563eb;">%s</a></p>
                    <p style="margin: 0;"><strong>Temporary Password:</strong> <code style="background: #e2e8f0; padding: 4px 8px; border-radius: 4px; font-family: monospace;">%s</code></p>
                </div>
                <p style="color: #6b7280; font-size: 14px;">For security purposes, you will be required to change your password upon your first login.</p>
                """.formatted(name, loginUrl, loginUrl, tempPassword);

        send(toEmail, "Invitation to join TaskFlow", buildEmailTemplate("Platform Invitation", content));
    }

    @Async
    public void sendPasswordReset(String toEmail, String token) {

        String link = baseUrl + "/auth/reset-password?token=" + token;
        String content = """
                <p>We received a request to reset the password associated with your TaskFlow account.</p>
                <p>If you made this request, please click the button below to set a new password:</p>
                <p style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="background-color: #0f172a; color: #ffffff; padding: 12px 28px; border-radius: 6px; text-decoration: none; font-weight: 600; display: inline-block;">Reset Password</a>
                </p>
                <p style="color: #6b7280; font-size: 14px; text-align: center;">This secure link will expire in 1 hour.</p>
                <p style="color: #6b7280; font-size: 14px; text-align: center;">If you did not request a password reset, please ignore this email or contact support if you have concerns.</p>
                """.formatted(link);

        send(toEmail, "Password Reset Request", buildEmailTemplate("Password Reset", content));
    }

    @Async
    public void sendAdminPasswordReset(String toEmail, String name, String tempPassword) {

        String loginUrl = baseUrl + "/auth/login";
        String content = """
                <p>Hello %s,</p>
                <p>An administrator has reset the access credentials for your TaskFlow account.</p>
                <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; padding: 16px; border-radius: 6px; margin: 20px 0;">
                    <p style="margin: 0;"><strong>New Temporary Password:</strong> <code style="background: #e2e8f0; padding: 4px 8px; border-radius: 4px; font-family: monospace;">%s</code></p>
                </div>
                <p style="color: #6b7280; font-size: 14px;">Please <a href="%s" style="color: #2563eb;">log in</a> immediately to establish a new, secure password.</p>
                """.formatted(name, tempPassword, loginUrl);

        send(toEmail, "TaskFlow Account Credential Update", buildEmailTemplate("Credentials Updated", content));
    }

    @Async
    public void sendTaskAssigned(String toEmail, String taskTitle, String projectName) {
        String content = """
                <p>You have been assigned to a new task.</p>
                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                    <tr>
                        <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0; color: #6b7280; width: 80px;">Project:</td>
                        <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0; font-weight: 600;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0; color: #6b7280;">Task:</td>
                        <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0; font-weight: 600;">%s</td>
                    </tr>
                </table>
                <p style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="background-color: #ffffff; color: #0f172a; border: 1px solid #0f172a; padding: 10px 24px; border-radius: 6px; text-decoration: none; font-weight: 600; display: inline-block;">View in TaskFlow</a>
                </p>
                """.formatted(projectName, taskTitle, baseUrl);

        send(toEmail, "New Task Assignment: " + taskTitle, buildEmailTemplate("Task Assigned", content));
    }

    @Async
    public void sendMentionNotification(String toEmail, String mentionedBy, String taskTitle) {
        String content = """
                <p><strong>%s</strong> has mentioned you in a comment regarding the following task:</p>
                <div style="background-color: #f8fafc; border-left: 4px solid #2563eb; padding: 12px 16px; margin: 20px 0;">
                    <p style="margin: 0; font-weight: 600;">%s</p>
                </div>
                <p style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="color: #2563eb; text-decoration: none; font-weight: 600;">&rarr; View Discussion in TaskFlow</a>
                </p>
                """.formatted(mentionedBy, taskTitle, baseUrl);

        send(toEmail, "Mention Notification from " + mentionedBy, buildEmailTemplate("Recent Mention", content));
    }

    @Async
    public void sendDueTomorrowReminder(String toEmail, String taskTitle) {
        String content = """
                <p>This is a courtesy reminder that you have an assigned task due tomorrow.</p>
                <div style="background-color: #fefce8; border: 1px solid #fef08a; padding: 16px; border-radius: 6px; margin: 20px 0;">
                    <p style="margin: 0; font-weight: 600; color: #854d0e;">%s</p>
                </div>
                <p style="text-align: center; margin: 20px 0;">
                    <a href="%s" style="color: #2563eb; text-decoration: none; font-weight: 600;">Review Task Details</a>
                </p>
                """.formatted(taskTitle, baseUrl);

        send(toEmail, "Reminder: Task Due Tomorrow", buildEmailTemplate("Upcoming Deadline", content));
    }

    @Async
    public void sendOverdueNotification(String toEmail, String taskTitle) {
        String content = """
                <p>Please be advised that the following assigned task is currently past its scheduled deadline.</p>
                <div style="background-color: #fef2f2; border: 1px solid #fecaca; padding: 16px; border-radius: 6px; margin: 20px 0;">
                    <p style="margin: 0; font-weight: 600; color: #991b1b;">%s</p>
                </div>
                <p style="text-align: center; margin: 20px 0;">
                    <a href="%s" style="color: #2563eb; text-decoration: none; font-weight: 600;">Review Task Details</a>
                </p>
                """.formatted(taskTitle, baseUrl);

        send(toEmail, "Action Required: Task Overdue", buildEmailTemplate("Task Overdue", content));
    }
    @Async
    public void sendProjectAssigned(String toEmail, String projectName) {
        String content = """
                <p>You have been added as a member to a new project.</p>
                <div style="background-color: #f8fafc; border-left: 4px solid #2563eb; padding: 12px 16px; margin: 20px 0;">
                    <p style="margin: 0; font-weight: 600;">%s</p>
                </div>
                <p style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="color: #2563eb; text-decoration: none; font-weight: 600;">&rarr; View Project in TaskFlow</a>
                </p>
                """.formatted(projectName, baseUrl);

        send(toEmail, "You were added to a project: " + projectName, buildEmailTemplate("Project Assignment", content));
    }

    @Async
    public void sendDependencyAdded(String toEmail, String taskTitle, String dependsOnTitle) {
        String content = """
                <p>A new dependency has been added to a task you are assigned to.</p>
                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                    <tr>
                        <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0; color: #6b7280; width: 120px;">Your Task:</td>
                        <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0; font-weight: 600;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0; color: #6b7280;">Now Depends On:</td>
                        <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0; font-weight: 600;">%s</td>
                    </tr>
                </table>
                <p style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="background-color: #ffffff; color: #0f172a; border: 1px solid #0f172a; padding: 10px 24px; border-radius: 6px; text-decoration: none; font-weight: 600; display: inline-block;">View in TaskFlow</a>
                </p>
                """.formatted(taskTitle, dependsOnTitle, baseUrl);

        send(toEmail, "Task Dependency Updated", buildEmailTemplate("Dependency Added", content));
    }
    // ── Private helpers ────────────────────────────────────────

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
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }


    private String buildEmailTemplate(String title, String content) {
        String template = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 0; background-color: #f3f4f6; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
                    <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="padding: 40px 20px;">
                        <tr>
                            <td align="center">
                                <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);">
                                    
                                    <tr>
                                        <td style="background-color: #0f172a; padding: 24px 32px; text-align: center;">
                                            <h1 style="color: #ffffff; margin: 0; font-size: 20px; font-weight: 600; letter-spacing: 0.5px;">TaskFlow</h1>
                                        </td>
                                    </tr>
                                    
                                    <tr>
                                        <td style="padding: 40px 32px; color: #334155; font-size: 16px; line-height: 1.6;">
                                            <h2 style="color: #0f172a; margin-top: 0; font-size: 20px;">{{TITLE}}</h2>
                                            {{CONTENT}}
                                        </td>
                                    </tr>
                                    
                                    <tr>
                                        <td style="background-color: #f8fafc; border-top: 1px solid #e2e8f0; padding: 24px 32px; text-align: center;">
                                            <p style="margin: 0; color: #64748b; font-size: 13px;">&copy; 2026 TaskFlow. All rights reserved.</p>
                                            <p style="margin: 8px 0 0 0; color: #94a3b8; font-size: 12px;">This is an automated message. Please do not reply directly to this email.</p>
                                        </td>
                                    </tr>
                                    
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """;

        return template
                .replace("{{TITLE}}", title)
                .replace("{{CONTENT}}", content);
    }
}