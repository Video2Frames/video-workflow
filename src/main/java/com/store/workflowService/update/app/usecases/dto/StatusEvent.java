package com.store.workflowService.update.app.usecases.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class StatusEvent {

    private String eventType;

    @JsonProperty("video_id")
    private String videoId;

    @JsonProperty("upload_path")
    private String uploadPath;

    @JsonProperty("uploaded_at")
    private Instant uploadedAt;

    @JsonProperty("processing_started_at")
    private Instant processingStartedAt;

    @JsonProperty("output_path")
    private String outputPath;

    @JsonProperty("processed_at")
    private Instant processedAt;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("failed_at")
    private Instant failedAt;

    @JsonProperty("user_id")
    private String userId;

    public StatusEvent() {
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Instant getProcessingStartedAt() {
        return processingStartedAt;
    }

    public void setProcessingStartedAt(Instant processingStartedAt) {
        this.processingStartedAt = processingStartedAt;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(Instant failedAt) {
        this.failedAt = failedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
