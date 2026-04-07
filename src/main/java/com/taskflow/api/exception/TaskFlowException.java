package com.taskflow.api.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class TaskFlowException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public TaskFlowException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}