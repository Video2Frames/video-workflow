package com.store.workflowService.upload.app.usecases;

import com.store.workflowService.upload.domain.model.Video;
import com.store.workflowService.upload.domain.model.VideoUploadResult;
import com.store.workflowService.upload.domain.repository.VideoEventPublisherPort;
import com.store.workflowService.upload.domain.repository.VideoStoragePort;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class UploadVideoUseCase {

    private final VideoStoragePort storagePort;
    private final VideoEventPublisherPort eventPublisherPort;

    public UploadVideoUseCase(VideoStoragePort storagePort,
                              VideoEventPublisherPort eventPublisherPort) {
        this.storagePort = storagePort;
        this.eventPublisherPort = eventPublisherPort;
    }

    public VideoUploadResult execute(Video video, InputStream content, long contentLength) {

        VideoUploadResult result = storagePort.upload(video, content, contentLength);

        // Best-effort: não deixa falha no SNS “estragar” um upload bem-sucedido no S3.
        eventPublisherPort.publishVideoUploaded(video, result);

        return result;
    }
}