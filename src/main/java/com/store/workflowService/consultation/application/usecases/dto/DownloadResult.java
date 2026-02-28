package com.store.workflowService.consultation.application.usecases.dto;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;

public class DownloadResult {
    private final InputStreamResource resource;
    private final MediaType mediaType;
    private final String fileName;

    public DownloadResult(InputStreamResource resource, MediaType mediaType, String fileName) {
        this.resource = resource;
        this.mediaType = mediaType;
        this.fileName = fileName;
    }

    public InputStreamResource getResource() { return resource; }
    public MediaType getMediaType() { return mediaType; }
    public String getFileName() { return fileName; }
}
