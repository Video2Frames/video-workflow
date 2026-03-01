package com.store.workflowService.upload.domain.model;

public class VideoUploadResult {

    private final String bucket;
    private final String key;
    private final String etag;

    public VideoUploadResult(String bucket, String key, String etag) {
        this.bucket = bucket;
        this.key = key;
        this.etag = etag;
    }

    public String getBucket() {
        return bucket;
    }

    public String getKey() {
        return key;
    }

    public String getEtag() {
        return etag;
    }
}
