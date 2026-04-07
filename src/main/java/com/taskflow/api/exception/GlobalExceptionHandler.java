package com.taskflow.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import com.taskflow.api.dto.response.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Our own exceptions ────────────────────────────────────────────────

    @ExceptionHandler(TaskFlowException.class)
    public ResponseEntity<ErrorResponse> handleTaskFlowException(
            TaskFlowException ex, HttpServletRequest request) {
        log.warn("TaskFlow exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        return buildResponse(ex.getStatus(), ex.getErrorCode(), ex.getMessage(), request);
    }

    private static final Set<String> AUTH_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/verify-email"
    );

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        // Auth endpoints — never reveal which field failed
        if (AUTH_PATHS.contains(request.getRequestURI())) {
            return buildResponse(
                    HttpStatus.UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "Invalid email or password",
                    request
            );
        }

        // All other endpoints — return helpful field-level errors
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

    // ── Spring Security exceptions ────────────────────────────────────────

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        // Deliberately vague — don't tell attackers which field was wrong
        return buildResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                "Invalid email or password", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "FORBIDDEN",
                "You do not have permission to perform this action", request);
    }

    // ── Catch-all ─────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(
            Exception ex, HttpServletRequest request) {
        // Log the full stack trace for unexpected errors
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later.", request);
    }

    // ── File size exceeded (attachment upload) ────────────────────

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.warn("File upload too large: {}", ex.getMessage());
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE",
                "File exceeds the maximum allowed size of 10MB", request);
    }

// ── 404 for unknown routes ────────────────────────────────────

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "ROUTE_NOT_FOUND",
                "The requested endpoint does not exist", request);
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String errorCode, String message, HttpServletRequest request) {

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .errorCode(errorCode)
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(body);
    }
}