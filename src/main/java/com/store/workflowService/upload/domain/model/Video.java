package com.store.workflowService.upload.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Video {

    private final String id;
    private final String userId;
    private final String fileName;
    private final OffsetDateTime uploadedAt;

    public Video(String userId, String fileName) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.fileName = fileName;
        this.uploadedAt = OffsetDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getFileName() {
        return fileName;
    }

    public OffsetDateTime getUploadedAt() {
        return uploadedAt;
    }
}
