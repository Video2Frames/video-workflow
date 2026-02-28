package com.store.workflowService.utils.exception.advice;

import com.store.workflowService.utils.exception.VideoNotFoundException;
import com.store.workflowService.utils.exception.VideoUploadException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(VideoNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(VideoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error("VIDEO_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(VideoUploadException.class)
    public ResponseEntity<Map<String, Object>> handleUpload(VideoUploadException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error("VIDEO_UPLOAD_FAILED", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleBodyValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = error("VALIDATION_ERROR", "Invalid request body");
        Map<String, String> fields = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fields.put(fe.getField(), fe.getDefaultMessage());
        }
        body.put("fields", fields);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleQueryValidation(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(error("VALIDATION_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error("INTERNAL_ERROR", "Unexpected error"));
    }

    private Map<String, Object> error(String code, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("code", code);
        body.put("message", message);
        return body;
    }
}