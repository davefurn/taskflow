package com.taskflow.api.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends TaskFlowException {
    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }
}