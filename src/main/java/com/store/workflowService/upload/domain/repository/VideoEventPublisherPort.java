package com.store.workflowService.upload.domain.repository;

import com.store.workflowService.upload.domain.model.Video;
import com.store.workflowService.upload.domain.model.VideoUploadResult;

public interface VideoEventPublisherPort {

    void publishVideoUploaded(Video video, VideoUploadResult uploadResult);

}