package com.taskflow.api.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ResourceNotFoundException extends TaskFlowException {
    public ResourceNotFoundException(String resource, UUID id) {
        super(resource + " not found with id: " + id, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}