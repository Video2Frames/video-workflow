package com.store.workflowService.upload.domain.repository;

import com.store.workflowService.upload.domain.model.Video;
import com.store.workflowService.upload.domain.model.VideoUploadResult;

import java.io.InputStream;

public interface VideoStoragePort {

    VideoUploadResult upload(Video video, InputStream content, long contentLength);

}