package com.store.workflowService.upload.infra.adapters.out.dto;

public record UploadVideoResponse(
        String bucket,
        String key,
        String etag
) {}