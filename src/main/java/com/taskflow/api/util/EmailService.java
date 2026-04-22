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
//package com.taskflow.api.util;
//
//import com.resend.Resend;
//import com.resend.core.exception.ResendException;
//import com.resend.services.emails.model.CreateEmailOptions;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import com.taskflow.api.entity.Task;
//import java.util.List;
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
//        this.resend  = new Resend(apiKey);
//        this.from    = from;
//        this.baseUrl = baseUrl;
//    }
//
//    @Async
//    public void sendEmailVerification(String toEmail, String token) {
//        String link = baseUrl + "/auth/verify-email?token=" + token;
//        String content = """
//                <p>Welcome to TaskFlow.</p>
//                <p>To complete your registration and secure your account, please verify your email address by clicking the button below.</p>
//                <p style="text-align: center; margin: 30px 0;">
//                    <a href="%s" style="background-color: #0f172a; color: #ffffff; padding: 12px 28px; border-radius: 6px; text-decoration: none; font-weight: 600; display: inline-block;">Verify Email Address</a>
//                </p>
//                <p style="color: #6b7280; font-size: 14px; text-align: center;">This verification link will expire in 24 hours.</p>
//                """.formatted(link);
//
//        send(toEmail, "Verify your TaskFlow Account", buildEmailTemplate("Account Verification", content));
//    }
//
//    @Async
//    public void sendInvitation(String toEmail, String name, String tempPassword) {
//
//        String loginUrl = baseUrl + "/auth/login";
//        String content = """
//                <p>Hello %s,</p>
//                <p>You have been invited to join the TaskFlow platform.</p>
//                <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; padding: 16px; border-radius: 6px; margin: 20px 0;">
//                    <p style="margin: 0 0 10px 0;"><strong>Login URL:</strong> <a href="%s" style="color: #2563eb;">%s</a></p>
//                    <p style="margin: 0;"><strong>Temporary Password:</strong> <code style="background: #e2e8f0; padding: 4px 8px; border-radius: 4px; font-family: monospace;">%s</code></p>
//                </div>
//                <p style="color: #6b7280; font-size: 14px;">For security purposes, you will be required to change your password upon your first login.</p>
//                """.formatted(name, loginUrl, loginUrl, tempPassword);
//
//        send(toEmail, "Invitation to join TaskFlow", buildEmailTemplate("Platform Invitation", content));
//    }
//
//    @Async
//    public void sendPasswordReset(String toEmail, String token) {
//
//        String link = baseUrl + "/auth/reset-password?token=" + token;
//        String content = """
//                <p>We received a request to reset the password associated with your TaskFlow account.</p>
//                <p>If you made this request, please click the button below to set a new password:</p>
//                <p style="text-align: center; margin: 30px 0;">
//                    <a href="%s" style="background-color: #0f172a; color: #ffffff; padding: 12px 28px; border-radius: 6px; text-decoration: none; font-weight: 600; display: inline-block;">Reset Password</a>
//                </p>
//                <p style="color: #6b7280; font-size: 14px; text-align: center;">This secure link will expire in 1 hour.</p>
//                <p style="color: #6b7280; font-size: 14px; text-align: center;">If you did not request a password reset, please ignore this email or contact support if you have concerns.</p>
//                """.formatted(link);
//
//        send(toEmail, "Password Reset Request", buildEmailTemplate("Password Reset", content));
//    }
//
//    @Async
//    public void sendAdminPasswordReset(String toEmail, String name, String tempPassword) {
//
//        String loginUrl = baseUrl + "/auth/login";
//        String content = """
//                <p>Hello %s,</p>
//                <p>An administrator has reset the access credentials for your TaskFlow account.</p>
//                <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; padding: 16px; border-radius: 6px; margin: 20px 0;">
//                    <p style="margin: 0;"><strong>New Temporary Password:</strong> <code style="background: #e2e8f0; padding: 4px 8px; border-radius: 4px; font-family: monospace;">%s</code></p>
//                </div>
//                <p style="color: #6b7280; font-size: 14px;">Please <a href="%s" style="color: #2563eb;">log in</a> immediately to establish a new, secure password.</p>
//                """.formatted(name, tempPassword, loginUrl);
//
//        send(toEmail, "TaskFlow Account Credential Update", buildEmailTemplate("Credentials Updated", content));
//    }
//
//    @Async
//    public void sendTaskAssigned(String toEmail, String taskTitle, String projectName) {
//        String content = """
//                <p>You have been assigned to a new task.</p>
//                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
//                    <tr>
//                        <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0; color: #6b7280; width: 80px;">Project:</td>
//                        <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0; font-weight: 600;">%s</td>
//                    </tr>
//                    <tr>
//                        <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0; color: #6b7280;">Task:</td>
//                        <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0; font-weight: 600;">%s</td>
//                    </tr>
//                </table>
//                <p style="text-align: center; margin: 30px 0;">
//                    <a href="%s" style="background-color: #ffffff; color: #0f172a; border: 1px solid #0f172a; padding: 10px 24px; border-radius: 6px; text-decoration: none; font-weight: 600; display: inline-block;">View in TaskFlow</a>
//                </p>
//                """.formatted(projectName, taskTitle, baseUrl);
//
//        send(toEmail, "New Task Assignment: " + taskTitle, buildEmailTemplate("Task Assigned", content));
//    }
//
//    @Async
//    public void sendMentionNotification(String toEmail, String mentionedBy, String taskTitle) {
//        String content = """
//                <p><strong>%s</strong> has mentioned you in a comment regarding the following task:</p>
//                <div style="background-color: #f8fafc; border-left: 4px solid #2563eb; padding: 12px 16px; margin: 20px 0;">
//                    <p style="margin: 0; font-weight: 600;">%s</p>
//                </div>
//                <p style="text-align: center; margin: 30px 0;">
//                    <a href="%s" style="color: #2563eb; text-decoration: none; font-weight: 600;">&rarr; View Discussion in TaskFlow</a>
//                </p>
//                """.formatted(mentionedBy, taskTitle, baseUrl);
//
//        send(toEmail, "Mention Notification from " + mentionedBy, buildEmailTemplate("Recent Mention", content));
//    }
//
////    @Async
////    public void sendDueTomorrowReminder(String toEmail, String taskTitle) {
////        String content = """
////                <p>This is a courtesy reminder that you have an assigned task due tomorrow.</p>
////                <div style="background-color: #fefce8; border: 1px solid #fef08a; padding: 16px; border-radius: 6px; margin: 20px 0;">
////                    <p style="margin: 0; font-weight: 600; color: #854d0e;">%s</p>
////                </div>
////                <p style="text-align: center; margin: 20px 0;">
////                    <a href="%s" style="color: #2563eb; text-decoration: none; font-weight: 600;">Review Task Details</a>
////                </p>
////                """.formatted(taskTitle, baseUrl);
////
////        send(toEmail, "Reminder: Task Due Tomorrow", buildEmailTemplate("Upcoming Deadline", content));
////    }
//
////    @Async
////    public void sendOverdueNotification(String toEmail, String taskTitle) {
////        String content = """
////                <p>Please be advised that the following assigned task is currently past its scheduled deadline.</p>
////                <div style="background-color: #fef2f2; border: 1px solid #fecaca; padding: 16px; border-radius: 6px; margin: 20px 0;">
////                    <p style="margin: 0; font-weight: 600; color: #991b1b;">%s</p>
////                </div>
////                <p style="text-align: center; margin: 20px 0;">
////                    <a href="%s" style="color: #2563eb; text-decoration: none; font-weight: 600;">Review Task Details</a>
////                </p>
////                """.formatted(taskTitle, baseUrl);
////
////        send(toEmail, "Action Required: Task Overdue", buildEmailTemplate("Task Overdue", content));
////    }
//    @Async
//    public void sendProjectAssigned(String toEmail, String projectName) {
//        String content = """
//                <p>You have been added as a member to a new project.</p>
//                <div style="background-color: #f8fafc; border-left: 4px solid #2563eb; padding: 12px 16px; margin: 20px 0;">
//                    <p style="margin: 0; font-weight: 600;">%s</p>
//                </div>
//                <p style="text-align: center; margin: 30px 0;">
//                    <a href="%s" style="color: #2563eb; text-decoration: none; font-weight: 600;">&rarr; View Project in TaskFlow</a>
//                </p>
//                """.formatted(projectName, baseUrl);
//
//        send(toEmail, "You were added to a project: " + projectName, buildEmailTemplate("Project Assignment", content));
//    }
//    @Async
//    public void sendWorkspaceAssigned(String toEmail, String workspaceName) {
//        String content = """
//                <p>You have been added as a member to a new workspace.</p>
//                <div style="background-color: #f8fafc; border-left: 4px solid #2563eb; padding: 12px 16px; margin: 20px 0;">
//                    <p style="margin: 0; font-weight: 600;">%s</p>
//                </div>
//                <p style="text-align: center; margin: 30px 0;">
//                    <a href="%s" style="color: #2563eb; text-decoration: none; font-weight: 600;">&rarr; View Workspace in TaskFlow</a>
//                </p>
//                """.formatted(workspaceName, baseUrl);
//
//        send(toEmail, "You were added to a workspace: " + workspaceName, buildEmailTemplate("Workspace Assignment", content));
//    }
//
//    @Async
//    public void sendDueTomorrowDigest(String toEmail, String userName,
//                                      List<Task> tasks) {
//        String subject = tasks.size() == 1
//                ? "Reminder: \"" + tasks.get(0).getTitle() + "\" is due tomorrow"
//                : "Reminder: " + tasks.size() + " tasks due tomorrow";
//
//        send(toEmail, subject, buildDueTomorrowHtml(userName, tasks));
//    }
//
//    @Async
//    public void sendOverdueDigest(String toEmail, String userName,
//                                  List<Task> tasks) {
//        String subject = tasks.size() == 1
//                ? "Overdue: \"" + tasks.get(0).getTitle() + "\""
//                : tasks.size() + " overdue tasks need your attention";
//
//        send(toEmail, subject, buildOverdueHtml(userName, tasks));
//    }
//
//// ── HTML builders ─────────────────────────────────────────
//
//    private String buildDueTomorrowHtml(String userName, List<Task> tasks) {
//        StringBuilder rows = new StringBuilder();
//        for (Task task : tasks) {
//            rows.append("<tr>")
//                    .append("<td style='padding:10px 12px;border-bottom:1px solid #f0f0f0'>")
//                    .append(escapeHtml(task.getTitle()))
//                    .append("</td>")
//                    .append("<td style='padding:10px 12px;border-bottom:1px solid #f0f0f0;color:#6b7280'>")
//                    .append(task.getProject() != null ? escapeHtml(task.getProject().getName()) : "—")
//                    .append("</td>")
//                    .append("<td style='padding:10px 12px;border-bottom:1px solid #f0f0f0;color:#6b7280'>")
//                    .append(priorityBadge(task.getPriority()))
//                    .append("</td>")
//                    .append("</tr>");
//        }
//
//        return "<div style='font-family:sans-serif;max-width:600px;margin:0 auto'>" +
//                "<h2 style='color:#111827'>Hi " + escapeHtml(userName) + ",</h2>" +
//                "<p style='color:#374151'>You have <strong>" + tasks.size() + " task" +
//                (tasks.size() > 1 ? "s" : "") + "</strong> due tomorrow:</p>" +
//                "<table style='width:100%;border-collapse:collapse;margin:16px 0'>" +
//                "<thead><tr>" +
//                "<th style='text-align:left;padding:8px 12px;background:#f9fafb;font-size:12px;color:#6b7280;text-transform:uppercase'>Task</th>" +
//                "<th style='text-align:left;padding:8px 12px;background:#f9fafb;font-size:12px;color:#6b7280;text-transform:uppercase'>Project</th>" +
//                "<th style='text-align:left;padding:8px 12px;background:#f9fafb;font-size:12px;color:#6b7280;text-transform:uppercase'>Priority</th>" +
//                "</tr></thead><tbody>" +
//                rows +
//                "</tbody></table>" +
//                "<a href='" + baseUrl + "/my-tasks' style='display:inline-block;" +
//                "background:#6366F1;color:white;padding:10px 20px;border-radius:6px;" +
//                "text-decoration:none;margin-top:8px'>View my tasks</a>" +
//                "<p style='color:#9ca3af;font-size:13px;margin-top:24px'>" +
//                "You can manage your notification preferences in TaskFlow settings.</p>" +
//                "</div>";
//    }
//
//    private String buildOverdueHtml(String userName, List<Task> tasks) {
//        StringBuilder rows = new StringBuilder();
//        for (Task task : tasks) {
//            long daysOverdue = task.getDueDate() != null
//                    ? java.time.temporal.ChronoUnit.DAYS.between(
//                    task.getDueDate(), java.time.LocalDate.now())
//                    : 0;
//
//            rows.append("<tr>")
//                    .append("<td style='padding:10px 12px;border-bottom:1px solid #f0f0f0'>")
//                    .append(escapeHtml(task.getTitle()))
//                    .append("</td>")
//                    .append("<td style='padding:10px 12px;border-bottom:1px solid #f0f0f0;color:#6b7280'>")
//                    .append(task.getProject() != null ? escapeHtml(task.getProject().getName()) : "—")
//                    .append("</td>")
//                    .append("<td style='padding:10px 12px;border-bottom:1px solid #f0f0f0;color:#ef4444;font-weight:500'>")
//                    .append(daysOverdue == 1 ? "1 day overdue" : daysOverdue + " days overdue")
//                    .append("</td>")
//                    .append("<td style='padding:10px 12px;border-bottom:1px solid #f0f0f0'>")
//                    .append(priorityBadge(task.getPriority()))
//                    .append("</td>")
//                    .append("</tr>");
//        }
//
//        return "<div style='font-family:sans-serif;max-width:600px;margin:0 auto'>" +
//                "<h2 style='color:#111827'>Hi " + escapeHtml(userName) + ",</h2>" +
//                "<p style='color:#374151'>You have <strong>" + tasks.size() + " overdue task" +
//                (tasks.size() > 1 ? "s" : "") + "</strong> that need your attention:</p>" +
//                "<table style='width:100%;border-collapse:collapse;margin:16px 0'>" +
//                "<thead><tr>" +
//                "<th style='text-align:left;padding:8px 12px;background:#fff7f7;font-size:12px;color:#6b7280;text-transform:uppercase'>Task</th>" +
//                "<th style='text-align:left;padding:8px 12px;background:#fff7f7;font-size:12px;color:#6b7280;text-transform:uppercase'>Project</th>" +
//                "<th style='text-align:left;padding:8px 12px;background:#fff7f7;font-size:12px;color:#6b7280;text-transform:uppercase'>Overdue by</th>" +
//                "<th style='text-align:left;padding:8px 12px;background:#fff7f7;font-size:12px;color:#6b7280;text-transform:uppercase'>Priority</th>" +
//                "</tr></thead><tbody>" +
//                rows +
//                "</tbody></table>" +
//                "<a href='" + baseUrl + "/my-tasks' style='display:inline-block;" +
//                "background:#ef4444;color:white;padding:10px 20px;border-radius:6px;" +
//                "text-decoration:none;margin-top:8px'>Review overdue tasks</a>" +
//                "<p style='color:#9ca3af;font-size:13px;margin-top:24px'>" +
//                "You can manage your notification preferences in TaskFlow settings.</p>" +
//                "</div>";
//    }
//
//
//
//    private String escapeHtml(String input) {
//        if (input == null) return "";
//        return input
//                .replace("&", "&amp;")
//                .replace("<", "&lt;")
//                .replace(">", "&gt;")
//                .replace("\"", "&quot;");
//    }
//    @Async
//    public void sendDependencyAdded(String toEmail, String taskTitle, String dependsOnTitle) {
//        String content = """
//                <p>A new dependency has been added to a task you are assigned to.</p>
//                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
//                    <tr>
//                        <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0; color: #6b7280; width: 120px;">Your Task:</td>
//                        <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0; font-weight: 600;">%s</td>
//                    </tr>
//                    <tr>
//                        <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0; color: #6b7280;">Now Depends On:</td>
//                        <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0; font-weight: 600;">%s</td>
//                    </tr>
//                </table>
//                <p style="text-align: center; margin: 30px 0;">
//                    <a href="%s" style="background-color: #ffffff; color: #0f172a; border: 1px solid #0f172a; padding: 10px 24px; border-radius: 6px; text-decoration: none; font-weight: 600; display: inline-block;">View in TaskFlow</a>
//                </p>
//                """.formatted(taskTitle, dependsOnTitle, baseUrl);
//
//        send(toEmail, "Task Dependency Updated", buildEmailTemplate("Dependency Added", content));
//    }
//    // ── Private helpers ────────────────────────────────────────
//// ── Helpers ───────────────────────────────────────────────
//
//    private String priorityBadge(Task.Priority priority) {
//        if (priority == null) return "<span style='color:#9ca3af'>None</span>";
//        return switch (priority) {
//            case urgent -> "<span style='color:#dc2626;font-weight:600'>Urgent</span>";
//            case high   -> "<span style='color:#ea580c;font-weight:500'>High</span>";
//            case medium -> "<span style='color:#d97706'>Medium</span>";
//            case low    -> "<span style='color:#65a30d'>Low</span>";
//            case none   -> "<span style='color:#9ca3af'>None</span>";
//        };
//    }
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
//            log.info("Email sent to {} — subject: {}", to, subject);
//
//        } catch (ResendException e) {
//            log.error("Failed to send email to {}: {}", to, e.getMessage());
//        }
//    }
//
//
//    private String buildEmailTemplate(String title, String content) {
//        String template = """
//                <!DOCTYPE html>
//                <html>
//                <head>
//                    <meta charset="UTF-8">
//                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
//                </head>
//                <body style="margin: 0; padding: 0; background-color: #f3f4f6; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
//                    <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="padding: 40px 20px;">
//                        <tr>
//                            <td align="center">
//                                <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);">
//
//                                    <tr>
//                                        <td style="background-color: #0f172a; padding: 24px 32px; text-align: center;">
//                                            <h1 style="color: #ffffff; margin: 0; font-size: 20px; font-weight: 600; letter-spacing: 0.5px;">TaskFlow</h1>
//                                        </td>
//                                    </tr>
//
//                                    <tr>
//                                        <td style="padding: 40px 32px; color: #334155; font-size: 16px; line-height: 1.6;">
//                                            <h2 style="color: #0f172a; margin-top: 0; font-size: 20px;">{{TITLE}}</h2>
//                                            {{CONTENT}}
//                                        </td>
//                                    </tr>
//
//                                    <tr>
//                                        <td style="background-color: #f8fafc; border-top: 1px solid #e2e8f0; padding: 24px 32px; text-align: center;">
//                                            <p style="margin: 0; color: #64748b; font-size: 13px;">&copy; 2026 TaskFlow. All rights reserved.</p>
//                                            <p style="margin: 8px 0 0 0; color: #94a3b8; font-size: 12px;">This is an automated message. Please do not reply directly to this email.</p>
//                                        </td>
//                                    </tr>
//
//                                </table>
//                            </td>
//                        </tr>
//                    </table>
//                </body>
//                </html>
//                """;
//
//        return template
//                .replace("{{TITLE}}", title)
//                .replace("{{CONTENT}}", content);
//    }
//}

package com.taskflow.api.util;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.taskflow.api.entity.Task;
import java.util.List;

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
                <p style="margin:0 0 16px;">Welcome to TaskFlow.</p>
                <p style="margin:0 0 24px;">To complete your registration and secure your account, please verify your email address by clicking the button below.</p>
                <p style="text-align:center;margin:32px 0;">
                    <a href="%s" style="display:inline-block;background-color:#0f172a;color:#ffffff;padding:12px 28px;border-radius:6px;text-decoration:none;font-size:14px;font-weight:600;letter-spacing:0.3px;">Verify Email Address &rarr;</a>
                </p>
                <p style="color:#94a3b8;font-size:13px;text-align:center;margin:0;">This verification link will expire in <strong>24 hours</strong>.</p>
                """.formatted(link);

        send(toEmail, "Verify your TaskFlow Account", buildEmailTemplate("Account Verification", content));
    }

    @Async
    public void sendInvitation(String toEmail, String name, String tempPassword) {
        String loginUrl = baseUrl + "/auth/login";
        String content = """
                <p style="margin:0 0 16px;">Hello <strong>%s</strong>,</p>
                <p style="margin:0 0 24px;">You have been invited to join the TaskFlow platform. Your account has been created and is ready to use.</p>

                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                       style="border:1px solid #e2e8f0;border-radius:8px;overflow:hidden;border-collapse:collapse;margin:0 0 24px;">
                    <tr style="background-color:#f8fafc;">
                        <td style="padding:12px 16px;font-size:12px;font-weight:600;color:#64748b;letter-spacing:0.7px;text-transform:uppercase;border-bottom:1px solid #e2e8f0;width:140px;">Login URL</td>
                        <td style="padding:12px 16px;font-size:14px;border-bottom:1px solid #e2e8f0;">
                            <a href="%s" style="color:#2563eb;text-decoration:none;">%s</a>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:12px 16px;font-size:12px;font-weight:600;color:#64748b;letter-spacing:0.7px;text-transform:uppercase;background-color:#f8fafc;">Temporary Password</td>
                        <td style="padding:12px 16px;font-size:14px;">
                            <code style="background:#f1f5f9;color:#0f172a;padding:4px 10px;border-radius:4px;font-family:monospace;font-size:14px;letter-spacing:0.5px;">%s</code>
                        </td>
                    </tr>
                </table>

                <p style="color:#64748b;font-size:13px;margin:0;">For security purposes, you will be required to change your password upon your first login.</p>
                """.formatted(name, loginUrl, loginUrl, tempPassword);

        send(toEmail, "You have been invited to TaskFlow", buildEmailTemplate("Platform Invitation", content));
    }

    @Async
    public void sendPasswordReset(String toEmail, String token) {
        String link = baseUrl + "/auth/reset-password?token=" + token;
        String content = """
                <p style="margin:0 0 16px;">We received a request to reset the password associated with your TaskFlow account.</p>
                <p style="margin:0 0 24px;">If you made this request, please click the button below to set a new password:</p>
                <p style="text-align:center;margin:32px 0;">
                    <a href="%s" style="display:inline-block;background-color:#0f172a;color:#ffffff;padding:12px 28px;border-radius:6px;text-decoration:none;font-size:14px;font-weight:600;letter-spacing:0.3px;">Reset Password &rarr;</a>
                </p>
                <p style="color:#94a3b8;font-size:13px;text-align:center;margin:0 0 8px;">This secure link will expire in <strong>1 hour</strong>.</p>
                <p style="color:#94a3b8;font-size:13px;text-align:center;margin:0;">If you did not request a password reset, please ignore this email or contact support if you have concerns.</p>
                """.formatted(link);

        send(toEmail, "Password Reset Request — TaskFlow", buildEmailTemplate("Password Reset", content));
    }

    @Async
    public void sendAdminPasswordReset(String toEmail, String name, String tempPassword) {
        String loginUrl = baseUrl + "/auth/login";
        String content = """
                <p style="margin:0 0 16px;">Hello <strong>%s</strong>,</p>
                <p style="margin:0 0 24px;">An administrator has reset the access credentials for your TaskFlow account. Please use the temporary password below to log in immediately.</p>

                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                       style="border:1px solid #e2e8f0;border-radius:8px;overflow:hidden;border-collapse:collapse;margin:0 0 24px;">
                    <tr>
                        <td style="padding:12px 16px;font-size:12px;font-weight:600;color:#64748b;letter-spacing:0.7px;text-transform:uppercase;background-color:#f8fafc;width:180px;">New Temporary Password</td>
                        <td style="padding:12px 16px;font-size:14px;">
                            <code style="background:#f1f5f9;color:#0f172a;padding:4px 10px;border-radius:4px;font-family:monospace;font-size:14px;letter-spacing:0.5px;">%s</code>
                        </td>
                    </tr>
                </table>

                <p style="color:#64748b;font-size:13px;margin:0;">
                    Please <a href="%s" style="color:#2563eb;text-decoration:none;font-weight:600;">log in</a> immediately and establish a new, secure password.
                </p>
                """.formatted(name, tempPassword, loginUrl);

        send(toEmail, "Your TaskFlow Credentials Have Been Updated", buildEmailTemplate("Credentials Updated", content));
    }

    @Async
    public void sendTaskAssigned(String toEmail, String taskTitle, String projectName) {
        String content = """
                <p style="margin:0 0 24px;">You have been assigned to a new task. Please review the details below and take any necessary action.</p>

                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                       style="border:1px solid #e2e8f0;border-radius:8px;overflow:hidden;border-collapse:collapse;margin:0 0 32px;">
                    <tr>
                        <td style="padding:12px 16px;font-size:12px;font-weight:600;color:#64748b;letter-spacing:0.7px;text-transform:uppercase;background-color:#f8fafc;border-bottom:1px solid #e2e8f0;width:100px;">Project</td>
                        <td style="padding:12px 16px;font-size:14px;color:#0f172a;font-weight:600;border-bottom:1px solid #e2e8f0;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding:12px 16px;font-size:12px;font-weight:600;color:#64748b;letter-spacing:0.7px;text-transform:uppercase;background-color:#f8fafc;">Task</td>
                        <td style="padding:12px 16px;font-size:14px;color:#0f172a;font-weight:600;">%s</td>
                    </tr>
                </table>

                <p style="text-align:center;margin:0;">
                    <a href="%s" style="display:inline-block;background-color:#0f172a;color:#ffffff;padding:12px 28px;border-radius:6px;text-decoration:none;font-size:14px;font-weight:600;letter-spacing:0.3px;">View Task in TaskFlow &rarr;</a>
                </p>
                """.formatted(projectName, taskTitle, baseUrl);

        send(toEmail, "New Task Assignment: " + taskTitle, buildEmailTemplate("Task Assigned", content));
    }

    @Async
    public void sendMentionNotification(String toEmail, String mentionedBy, String taskTitle) {
        String content = """
                <p style="margin:0 0 24px;"><strong>%s</strong> has mentioned you in a comment on the following task:</p>

                <div style="background-color:#f8fafc;border-left:4px solid #2563eb;border-radius:0 6px 6px 0;padding:14px 18px;margin:0 0 32px;">
                    <p style="margin:0;font-size:15px;font-weight:600;color:#0f172a;">%s</p>
                </div>

                <p style="text-align:center;margin:0;">
                    <a href="%s" style="display:inline-block;color:#2563eb;text-decoration:none;font-size:14px;font-weight:600;">&rarr; View Discussion in TaskFlow</a>
                </p>
                """.formatted(mentionedBy, taskTitle, baseUrl);

        send(toEmail, mentionedBy + " mentioned you in a comment", buildEmailTemplate("You Were Mentioned", content));
    }

    @Async
    public void sendProjectAssigned(String toEmail, String projectName) {
        String content = """
                <p style="margin:0 0 24px;">You have been added as a member to a new project on TaskFlow.</p>

                <div style="background-color:#f8fafc;border-left:4px solid #2563eb;border-radius:0 6px 6px 0;padding:14px 18px;margin:0 0 32px;">
                    <p style="margin:0;font-size:15px;font-weight:600;color:#0f172a;">%s</p>
                </div>

                <p style="text-align:center;margin:0;">
                    <a href="%s" style="display:inline-block;background-color:#0f172a;color:#ffffff;padding:12px 28px;border-radius:6px;text-decoration:none;font-size:14px;font-weight:600;letter-spacing:0.3px;">View Project &rarr;</a>
                </p>
                """.formatted(projectName, baseUrl);

        send(toEmail, "You were added to a project: " + projectName, buildEmailTemplate("Project Assignment", content));
    }

    @Async
    public void sendWorkspaceAssigned(String toEmail, String workspaceName) {
        String content = """
                <p style="margin:0 0 24px;">You have been added as a member to a new workspace on TaskFlow.</p>

                <div style="background-color:#f8fafc;border-left:4px solid #2563eb;border-radius:0 6px 6px 0;padding:14px 18px;margin:0 0 32px;">
                    <p style="margin:0;font-size:15px;font-weight:600;color:#0f172a;">%s</p>
                </div>

                <p style="text-align:center;margin:0;">
                    <a href="%s" style="display:inline-block;background-color:#0f172a;color:#ffffff;padding:12px 28px;border-radius:6px;text-decoration:none;font-size:14px;font-weight:600;letter-spacing:0.3px;">View Workspace &rarr;</a>
                </p>
                """.formatted(workspaceName, baseUrl);

        send(toEmail, "You were added to a workspace: " + workspaceName, buildEmailTemplate("Workspace Assignment", content));
    }

    @Async
    public void sendDependencyAdded(String toEmail, String taskTitle, String dependsOnTitle) {
        String content = """
                <p style="margin:0 0 24px;">A new dependency has been added to a task you are assigned to. Please review and ensure the dependent task is accounted for in your planning.</p>

                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                       style="border:1px solid #e2e8f0;border-radius:8px;overflow:hidden;border-collapse:collapse;margin:0 0 32px;">
                    <tr>
                        <td style="padding:12px 16px;font-size:12px;font-weight:600;color:#64748b;letter-spacing:0.7px;text-transform:uppercase;background-color:#f8fafc;border-bottom:1px solid #e2e8f0;width:150px;">Your Task</td>
                        <td style="padding:12px 16px;font-size:14px;color:#0f172a;font-weight:600;border-bottom:1px solid #e2e8f0;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding:12px 16px;font-size:12px;font-weight:600;color:#64748b;letter-spacing:0.7px;text-transform:uppercase;background-color:#f8fafc;">Now Depends On</td>
                        <td style="padding:12px 16px;font-size:14px;color:#0f172a;font-weight:600;">%s</td>
                    </tr>
                </table>

                <p style="text-align:center;margin:0;">
                    <a href="%s" style="display:inline-block;background-color:#0f172a;color:#ffffff;padding:12px 28px;border-radius:6px;text-decoration:none;font-size:14px;font-weight:600;letter-spacing:0.3px;">View in TaskFlow &rarr;</a>
                </p>
                """.formatted(taskTitle, dependsOnTitle, baseUrl);

        send(toEmail, "Task Dependency Updated: " + taskTitle, buildEmailTemplate("Dependency Added", content));
    }

    @Async
    public void sendDueTomorrowDigest(String toEmail, String userName, List<Task> tasks) {
        String subject = tasks.size() == 1
                ? "Reminder: \"" + tasks.get(0).getTitle() + "\" is due tomorrow"
                : "Reminder: " + tasks.size() + " tasks due tomorrow";

        send(toEmail, subject, buildDueTomorrowHtml(userName, tasks));
    }

    @Async
    public void sendOverdueDigest(String toEmail, String userName, List<Task> tasks) {
        String subject = tasks.size() == 1
                ? "Overdue: \"" + tasks.get(0).getTitle() + "\""
                : tasks.size() + " overdue tasks need your attention";

        send(toEmail, subject, buildOverdueHtml(userName, tasks));
    }

    // ── HTML digest builders ───────────────────────────────────

    private String buildDueTomorrowHtml(String userName, List<Task> tasks) {
        StringBuilder rows = new StringBuilder();
        for (Task task : tasks) {
            rows.append("""
                <tr>
                    <td style="padding:12px 14px;border-bottom:1px solid #f1f5f9;font-size:14px;color:#0f172a;">%s</td>
                    <td style="padding:12px 14px;border-bottom:1px solid #f1f5f9;font-size:14px;color:#64748b;">%s</td>
                    <td style="padding:12px 14px;border-bottom:1px solid #f1f5f9;font-size:14px;">%s</td>
                </tr>
                """.formatted(
                    escapeHtml(task.getTitle()),
                    task.getProject() != null ? escapeHtml(task.getProject().getName()) : "—",
                    priorityBadge(task.getPriority())
            ));
        }

        String content = """
                <p style="margin:0 0 8px;">Hello <strong>%s</strong>,</p>
                <p style="margin:0 0 24px;color:#374151;">You have <strong>%d task%s</strong> due tomorrow. Please review and ensure they are on track before the deadline.</p>

                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                       style="border:1px solid #e2e8f0;border-radius:8px;overflow:hidden;border-collapse:collapse;margin:0 0 32px;">
                    <thead>
                        <tr style="background-color:#f8fafc;">
                            <th style="padding:10px 14px;text-align:left;font-size:11px;font-weight:600;color:#64748b;letter-spacing:0.7px;text-transform:uppercase;border-bottom:1px solid #e2e8f0;">Task</th>
                            <th style="padding:10px 14px;text-align:left;font-size:11px;font-weight:600;color:#64748b;letter-spacing:0.7px;text-transform:uppercase;border-bottom:1px solid #e2e8f0;">Project</th>
                            <th style="padding:10px 14px;text-align:left;font-size:11px;font-weight:600;color:#64748b;letter-spacing:0.7px;text-transform:uppercase;border-bottom:1px solid #e2e8f0;">Priority</th>
                        </tr>
                    </thead>
                    <tbody>%s</tbody>
                </table>

                <p style="text-align:center;margin:0;">
                    <a href="%s/my-tasks" style="display:inline-block;background-color:#0f172a;color:#ffffff;padding:12px 28px;border-radius:6px;text-decoration:none;font-size:14px;font-weight:600;letter-spacing:0.3px;">View My Tasks &rarr;</a>
                </p>
                """.formatted(
                escapeHtml(userName),
                tasks.size(), tasks.size() == 1 ? "" : "s",
                rows,
                baseUrl
        );

        return buildEmailTemplate("Upcoming Deadline Reminder", content);
    }

    private String buildOverdueHtml(String userName, List<Task> tasks) {
        StringBuilder rows = new StringBuilder();
        for (Task task : tasks) {
            long daysOverdue = task.getDueDate() != null
                    ? java.time.temporal.ChronoUnit.DAYS.between(task.getDueDate(), java.time.LocalDate.now())
                    : 0;

            rows.append("""
                <tr>
                    <td style="padding:12px 14px;border-bottom:1px solid #fef2f2;font-size:14px;color:#0f172a;">%s</td>
                    <td style="padding:12px 14px;border-bottom:1px solid #fef2f2;font-size:14px;color:#64748b;">%s</td>
                    <td style="padding:12px 14px;border-bottom:1px solid #fef2f2;font-size:14px;font-weight:600;color:#dc2626;">%s</td>
                    <td style="padding:12px 14px;border-bottom:1px solid #fef2f2;font-size:14px;">%s</td>
                </tr>
                """.formatted(
                    escapeHtml(task.getTitle()),
                    task.getProject() != null ? escapeHtml(task.getProject().getName()) : "—",
                    daysOverdue == 1 ? "1 day overdue" : daysOverdue + " days overdue",
                    priorityBadge(task.getPriority())
            ));
        }

        String content = """
                <p style="margin:0 0 8px;">Hello <strong>%s</strong>,</p>
                <p style="margin:0 0 24px;color:#374151;">The following <strong>%d task%s</strong> %s past the scheduled deadline and require immediate attention.</p>

                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                       style="border:1px solid #fecaca;border-radius:8px;overflow:hidden;border-collapse:collapse;margin:0 0 32px;">
                    <thead>
                        <tr style="background-color:#fff7f7;">
                            <th style="padding:10px 14px;text-align:left;font-size:11px;font-weight:600;color:#64748b;letter-spacing:0.7px;text-transform:uppercase;border-bottom:1px solid #fecaca;">Task</th>
                            <th style="padding:10px 14px;text-align:left;font-size:11px;font-weight:600;color:#64748b;letter-spacing:0.7px;text-transform:uppercase;border-bottom:1px solid #fecaca;">Project</th>
                            <th style="padding:10px 14px;text-align:left;font-size:11px;font-weight:600;color:#64748b;letter-spacing:0.7px;text-transform:uppercase;border-bottom:1px solid #fecaca;">Overdue By</th>
                            <th style="padding:10px 14px;text-align:left;font-size:11px;font-weight:600;color:#64748b;letter-spacing:0.7px;text-transform:uppercase;border-bottom:1px solid #fecaca;">Priority</th>
                        </tr>
                    </thead>
                    <tbody>%s</tbody>
                </table>

                <p style="text-align:center;margin:0;">
                    <a href="%s/my-tasks" style="display:inline-block;background-color:#dc2626;color:#ffffff;padding:12px 28px;border-radius:6px;text-decoration:none;font-size:14px;font-weight:600;letter-spacing:0.3px;">Review Overdue Tasks &rarr;</a>
                </p>
                """.formatted(
                escapeHtml(userName),
                tasks.size(), tasks.size() == 1 ? "" : "s",
                tasks.size() == 1 ? "is" : "are",
                rows,
                baseUrl
        );

        return buildEmailTemplate("Overdue Task Alert", content);
    }

    // ── Private helpers ────────────────────────────────────────

    private String priorityBadge(Task.Priority priority) {
        if (priority == null) return "<span style='color:#9ca3af'>None</span>";
        return switch (priority) {
            case urgent -> "<span style='color:#dc2626;font-weight:600'>Urgent</span>";
            case high   -> "<span style='color:#ea580c;font-weight:500'>High</span>";
            case medium -> "<span style='color:#d97706'>Medium</span>";
            case low    -> "<span style='color:#65a30d'>Low</span>";
            case none   -> "<span style='color:#9ca3af'>None</span>";
        };
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

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
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s — TaskFlow</title>
                </head>
                <body style="margin:0;padding:0;background-color:#eef2f7;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;">
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="padding:48px 16px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:600px;">

                                    <!-- Header -->
                                    <tr>
                                        <td style="background-color:#0f172a;border-radius:10px 10px 0 0;padding:24px 40px;">
                                            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0">
                                                <tr>
                                                    <td style="text-align:left;">
                                                        <span style="color:#ffffff;font-size:18px;font-weight:700;letter-spacing:0.3px;">TaskFlow</span>
                                                    </td>
                                                    <td style="text-align:right;">
                                                        <span style="color:#94a3b8;font-size:11px;letter-spacing:0.8px;text-transform:uppercase;">Workspace Notifications</span>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>

                                    <!-- Title band -->
                                    <tr>
                                        <td style="background-color:#1e293b;padding:12px 40px;">
                                            <span style="color:#e2e8f0;font-size:11px;font-weight:600;letter-spacing:1px;text-transform:uppercase;">%s</span>
                                        </td>
                                    </tr>

                                    <!-- Body -->
                                    <tr>
                                        <td style="background-color:#ffffff;padding:40px 40px 36px;color:#334155;font-size:15px;line-height:1.7;">
                                            %s
                                        </td>
                                    </tr>

                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color:#f8fafc;border-top:1px solid #e2e8f0;border-radius:0 0 10px 10px;padding:24px 40px;">
                                            <p style="margin:0;color:#64748b;font-size:12px;line-height:1.6;">
                                                This is an automated notification from <strong style="color:#0f172a;">TaskFlow</strong>.
                                                Please do not reply to this email.
                                            </p>
                                            <p style="margin:8px 0 0;color:#94a3b8;font-size:11px;">
                                                &copy; 2026 TaskFlow, Inc. &nbsp;&bull;&nbsp; All rights reserved.
                                            </p>
                                        </td>
                                    </tr>

                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(title, title, content);
    }
}