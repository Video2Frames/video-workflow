package com.store.workflowService.utils.exception;

public class VideoUploadException extends RuntimeException {
    public VideoUploadException(String message) {
        super(message);
    }
    public VideoUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}