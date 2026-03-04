package com.store.workflowService.update.app.service;

import com.store.workflowService.update.app.usecases.UpdateStatusUseCase;
import com.store.workflowService.update.app.usecases.dto.StatusEvent;
import com.store.workflowService.update.domain.model.VideoWorkflow;
import com.store.workflowService.update.domain.repository.VideoWorkflowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class UpdateVideoService implements UpdateStatusUseCase {

    private final VideoWorkflowRepository repository;
    private static final Logger log = LoggerFactory.getLogger(UpdateVideoService.class);


    public UpdateVideoService(VideoWorkflowRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public VideoWorkflow updateStatus(StatusEvent event) {
        String eventType = event.getEventType();
        String videoId = event.getVideoId();

        if (videoId == null) {
            throw new IllegalArgumentException("Status event must contain video_id");
        }

        log.info("Verificando se videoId existe: videoId={}", videoId);
        Optional<VideoWorkflow> existing = repository.findByVideoId(videoId);

        if (existing.isEmpty() && !"video.uploaded".equals(eventType)) {
            log.warn("Recebido evento {} para videoId={} mas não existe registro; ignorando (aguardando video.uploaded)", eventType, videoId);
            return null;
        }

        VideoWorkflow wf = existing.orElseGet(VideoWorkflow::new);

        // Ensure videoId is set on new entities
        wf.setVideoId(videoId);

        switch (eventType) {
            case "video.uploaded" -> {
                log.info("Ignorando evento video.uploaded para videoId={}", videoId);
                return null;
            }
            case "video.processing_started" -> wf.setStatus("PROCESSING");
            case "video.processed" -> {
                wf.setOutputPath(event.getOutputPath());
                wf.setProcessedAt(event.getProcessedAt());
                wf.setStatus("PROCESSED");
            }
            case "video.processing_failed" -> {
                wf.setStatus("FAILED");
                wf.setErrorMessage(event.getErrorMessage());
                wf.setFailedAt(event.getFailedAt() != null ? event.getFailedAt() : Instant.now());
            }
            default -> wf.setStatus("UNKNOWN");
        }

        log.info("Atualizando status para {}: videoId={}", wf.getStatus(), videoId);
        return repository.save(wf);
    }

}
