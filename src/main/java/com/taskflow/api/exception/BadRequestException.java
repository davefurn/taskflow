package com.taskflow.api.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends TaskFlowException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }
}