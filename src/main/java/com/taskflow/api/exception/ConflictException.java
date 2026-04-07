package com.taskflow.api.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends TaskFlowException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "CONFLICT");
    }
}