package com.taskflow.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import com.taskflow.api.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Set<String> AUTH_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/verify-email"
    );

    // ── Our own exceptions ────────────────────────────────────

    @ExceptionHandler(TaskFlowException.class)
    public ResponseEntity<ErrorResponse> handleTaskFlowException(
            TaskFlowException ex, HttpServletRequest request) {
        log.warn("TaskFlow exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        return buildResponse(ex.getStatus(), ex.getErrorCode(),
                ex.getMessage(), request);
    }

    // ── @Valid annotation failures ────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        if (AUTH_PATHS.contains(request.getRequestURI())) {
            return buildResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                    "Invalid email or password", request);
        }

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Validation failed on {}: {}", request.getRequestURI(), fieldErrors);

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("VALIDATION_ERROR")
                .message("Request validation failed")
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    // ── Type mismatch — e.g. string passed where UUID expected ─

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String paramName  = ex.getName();
        String givenValue = ex.getValue() != null ? ex.getValue().toString() : "null";
        String expected   = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName() : "unknown";

        String message = String.format(
                "Invalid value '%s' for parameter '%s' — expected type: %s",
                givenValue, paramName, expected);

        log.warn("Type mismatch on {}: {}", request.getRequestURI(), message);
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER",
                message, request);
    }

    // ── Missing required request parameter ───────────────────

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        String message = String.format(
                "Required parameter '%s' of type '%s' is missing",
                ex.getParameterName(), ex.getParameterType());

        return buildResponse(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER",
                message, request);
    }

    // ── Malformed JSON body ───────────────────────────────────

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        String message = "Malformed request body — check your JSON format and field types.";

        // Detect UUID parse errors in body
        String cause = ex.getMessage();
        if (cause != null && cause.contains("UUID")) {
            message = "Invalid UUID format in request body. " +
                    "Expected format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
        } else if (cause != null && cause.contains("not one of the values accepted")) {
            message = extractEnumError(cause);
        } else if (cause != null && cause.contains("LocalDate")) {
            message = "Invalid date format. Expected: YYYY-MM-DD";
        }

        log.warn("Unreadable message on {}: {}", request.getRequestURI(),
                ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY",
                message, request);
    }

    // ── Database constraint violations ────────────────────────

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        String message = "Request could not be completed due to a data conflict.";
        String cause = ex.getMessage();

        if (cause != null) {
            if (cause.contains("duplicate key") || cause.contains("unique")) {
                message = extractDuplicateKeyMessage(cause);
            } else if (cause.contains("foreign key") || cause.contains("violates")) {
                message = extractForeignKeyMessage(cause);
            } else if (cause.contains("not-null") || cause.contains("null value")) {
                message = "A required field is missing or null.";
            }
        }

        log.warn("Data integrity violation on {}: {}",
                request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "DATA_CONFLICT", message, request);
    }

    // ── File size exceeded ────────────────────────────────────

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE",
                "File exceeds the maximum allowed size of 10MB.", request);
    }

    // ── Unknown route ─────────────────────────────────────────

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "ROUTE_NOT_FOUND",
                "The requested endpoint does not exist: " + request.getRequestURI(),
                request);
    }

    // ── Spring Security ───────────────────────────────────────

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                "Invalid email or password", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "FORBIDDEN",
                "You do not have permission to perform this action.", request);
    }

    // ── Catch-all ─────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {}: {}", request.getRequestURI(),
                ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later.", request);
    }

    // ── Helpers ───────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String errorCode,
            String message, HttpServletRequest request) {

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .errorCode(errorCode)
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(body);
    }

    private String extractDuplicateKeyMessage(String cause) {
        if (cause.contains("users_email_key") || cause.contains("email")) {
            return "A user with this email address already exists.";
        }
        if (cause.contains("task_dependencies")) {
            return "This dependency already exists between these tasks.";
        }
        if (cause.contains("workspace_members")) {
            return "This user is already a member of this workspace.";
        }
        if (cause.contains("project_members")) {
            return "This user is already a member of this project.";
        }
        return "A record with this value already exists.";
    }

    private String extractForeignKeyMessage(String cause) {
        if (cause.contains("status_id") || cause.contains("task_statuses")) {
            return "The specified status ID does not exist or does not belong to this project.";
        }
        if (cause.contains("project_id") || cause.contains("projects")) {
            return "The specified project does not exist.";
        }
        if (cause.contains("workspace_id") || cause.contains("workspaces")) {
            return "The specified workspace does not exist.";
        }
        if (cause.contains("user_id") || cause.contains("users")) {
            return "The specified user does not exist.";
        }
        if (cause.contains("task_group_id") || cause.contains("task_groups")) {
            return "The specified task group does not exist.";
        }
        if (cause.contains("label_id") || cause.contains("labels")) {
            return "The specified label does not exist or does not belong to this project.";
        }
        if (cause.contains("parent_task_id")) {
            return "The specified parent task does not exist.";
        }
        return "A referenced record does not exist. " +
                "Check that all IDs in the request are valid.";
    }

    private String extractEnumError(String cause) {
        if (cause.contains("Priority")) {
            return "Invalid priority value. Allowed: urgent, high, medium, low, none";
        }
        if (cause.contains("Role")) {
            return "Invalid role value. Allowed: admin, manager, member, viewer";
        }
        if (cause.contains("Status") || cause.contains("status")) {
            return "Invalid status value. " +
                    "Allowed: not_started, in_progress, on_hold, completed, archived";
        }
        if (cause.contains("DependencyType")) {
            return "Invalid dependency type. Allowed: blocked_by, related_to";
        }
        return "Invalid enum value in request body. Check allowed values.";
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(v -> {
            String field = v.getPropertyPath().toString();
            fieldErrors.put(field, v.getMessage());
        });

        log.warn("Constraint violation on {}: {}", request.getRequestURI(), fieldErrors);

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("VALIDATION_ERROR")
                .message("Request validation failed")
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(body);
    }
}