package com.store.workflowService.consultation.application.usecases.dto;

import java.time.Instant;

public class VideoDto {
    private String videoId;
    private String userId;
    private String uploadPath;
    private String outputPath;
    private String status;
    private Instant uploadedAt;
    private Instant processedAt;

    public VideoDto() {}

    public VideoDto(String videoId, String userId, String uploadPath, String outputPath, String status, Instant uploadedAt, Instant processedAt) {
        this.videoId = videoId;
        this.userId = userId;
        this.uploadPath = uploadPath;
        this.outputPath = outputPath;
        this.status = status;
        this.uploadedAt = uploadedAt;
        this.processedAt = processedAt;
    }

    public String getVideoId() { return videoId; }
    public String getUserId() { return userId; }
    public String getUploadPath() { return uploadPath; }
    public String getOutputPath() { return outputPath; }
    public String getStatus() { return status; }
    public Instant getUploadedAt() { return uploadedAt; }
    public Instant getProcessedAt() { return processedAt; }
}

