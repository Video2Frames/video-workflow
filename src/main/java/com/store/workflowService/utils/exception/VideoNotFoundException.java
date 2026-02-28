package com.store.workflowService.utils.exception;

public class VideoNotFoundException extends RuntimeException {
    public VideoNotFoundException(String message) {
        super(message);
    }
}
