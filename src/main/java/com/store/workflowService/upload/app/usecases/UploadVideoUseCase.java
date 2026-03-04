package com.store.workflowService.upload.app.usecases;

import com.store.workflowService.upload.domain.model.Video;
import com.store.workflowService.upload.domain.model.VideoUploadResult;
import com.store.workflowService.upload.domain.repository.VideoEventPublisherPort;
import com.store.workflowService.upload.domain.repository.VideoStoragePort;
import com.store.workflowService.update.domain.model.VideoWorkflow;
import com.store.workflowService.update.domain.repository.VideoWorkflowRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Optional;

@Component
public class UploadVideoUseCase {

    private final VideoStoragePort storagePort;
    private final VideoEventPublisherPort eventPublisherPort;
    private final VideoWorkflowRepository workflowRepository;

    public UploadVideoUseCase(VideoStoragePort storagePort,
                              VideoEventPublisherPort eventPublisherPort,
                              VideoWorkflowRepository workflowRepository) {
        this.storagePort = storagePort;
        this.eventPublisherPort = eventPublisherPort;
        this.workflowRepository = workflowRepository;
    }

    @Transactional
    public VideoUploadResult execute(Video video, InputStream content, long contentLength) {

        VideoUploadResult result = storagePort.upload(video, content, contentLength);

        eventPublisherPort.publishVideoUploaded(video, result);

        // Persist or update workflow status to UPLOADED (mirror UpdateVideoService behavior)
        if (result != null) {
            String videoId = video.getId();
            Optional<VideoWorkflow> existing = workflowRepository.findByVideoId(videoId);
            VideoWorkflow wf = existing.orElseGet(VideoWorkflow::new);

            wf.setVideoId(videoId);
            wf.setUserId(video.getUserId());
            wf.setUploadPath("s3://" + result.getBucket() + "/" + result.getKey());
            // convert OffsetDateTime to Instant
            if (video.getUploadedAt() != null) {
                wf.setUploadedAt(video.getUploadedAt().toInstant());
            }
            wf.setStatus("UPLOADED");

            workflowRepository.save(wf);
        }

        return result;
    }
}