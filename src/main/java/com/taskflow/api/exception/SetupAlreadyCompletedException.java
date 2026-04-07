package com.taskflow.api.exception;

import org.springframework.http.HttpStatus;

public class SetupAlreadyCompletedException extends TaskFlowException {
    public SetupAlreadyCompletedException() {
        super(
                "Setup already completed. An admin account already exists.",
                HttpStatus.CONFLICT,
                "SETUP_ALREADY_COMPLETED"
        );
    }
}