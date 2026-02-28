package com.store.workflowService.upload.infra.adapters.in.dto;

import jakarta.validation.constraints.NotBlank;

public record UploadVideoRequest(
        @NotBlank String userId,
        @NotBlank String videoPath
) {}