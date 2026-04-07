package com.taskflow.api.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends TaskFlowException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}