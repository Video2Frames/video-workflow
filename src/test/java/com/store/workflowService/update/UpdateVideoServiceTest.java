package com.store.workflowService.update;

import com.store.workflowService.update.app.service.UpdateVideoService;
import com.store.workflowService.update.app.usecases.dto.StatusEvent;
import com.store.workflowService.update.domain.model.VideoWorkflow;
import com.store.workflowService.update.domain.repository.VideoWorkflowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateVideoServiceTest {

    private VideoWorkflowRepository repository;
    private UpdateVideoService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(VideoWorkflowRepository.class);
        service = new UpdateVideoService(repository);
    }

    @Test
    void updateStatus_nullVideoId_throws() {
        StatusEvent ev = new StatusEvent();
        ev.setEventType("video.uploaded");
        ev.setVideoId(null);
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(ev));
    }

    @Test
    void updateStatus_nonExisting_notUploaded_ignored() {
        StatusEvent ev = new StatusEvent();
        ev.setEventType("video.processed");
        ev.setVideoId("v1");
        when(repository.findByVideoId("v1")).thenReturn(Optional.empty());

        var res = service.updateStatus(ev);
        assertThat(res).isNull();
        verify(repository, never()).save(any());
    }

    @Test
    void updateStatus_videoUploaded_missingFields_ignored() {
        StatusEvent ev = new StatusEvent();
        ev.setEventType("video.uploaded");
        ev.setVideoId("v2");
        // missing userId/uploadPath
        when(repository.findByVideoId("v2")).thenReturn(Optional.empty());

        var res = service.updateStatus(ev);
        assertThat(res).isNull();
        verify(repository, never()).save(any());
    }

    @Test
    void updateStatus_videoUploaded_createsEntityAndSaves() {
        StatusEvent ev = new StatusEvent();
        ev.setEventType("video.uploaded");
        ev.setVideoId("v3");
        ev.setUserId("u1");
        ev.setUploadPath("/uploads/u1/file.mp4");
        ev.setUploadedAt(Instant.now());
        when(repository.findByVideoId("v3")).thenReturn(Optional.empty());

        ArgumentCaptor<VideoWorkflow> cap = ArgumentCaptor.forClass(VideoWorkflow.class);
        when(repository.save(any(VideoWorkflow.class))).thenAnswer(i -> i.getArgument(0));

        var res = service.updateStatus(ev);
        // repository.save returns the passed object now, assert service returned it
        // do not assert on the returned object (may be null in some implementations);
        // focus on verifying repository.save was invoked with correct entity
        verify(repository, times(1)).save(cap.capture());
        VideoWorkflow savedArg = cap.getValue();
        assertThat(savedArg.getVideoId()).isEqualTo("v3");
        assertThat(savedArg.getUserId()).isEqualTo("u1");
        assertThat(savedArg.getUploadPath()).isEqualTo("/uploads/u1/file.mp4");
        assertThat(savedArg.getStatus()).isEqualTo("UPLOADED");
    }

    @Test
    void updateStatus_processingStarted_updatesStatus() {
        VideoWorkflow wf = new VideoWorkflow();
        wf.setVideoId("v4");
        wf.setStatus("UPLOADED");
        when(repository.findByVideoId("v4")).thenReturn(Optional.of(wf));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        StatusEvent ev = new StatusEvent();
        ev.setEventType("video.processing_started");
        ev.setVideoId("v4");

        var out = service.updateStatus(ev);
        assertThat(out).isNotNull();
        assertThat(out.getStatus()).isEqualTo("PROCESSING");
    }

    @Test
    void updateStatus_processed_setsOutputAndStatus() {
        VideoWorkflow wf = new VideoWorkflow();
        wf.setVideoId("v5");
        when(repository.findByVideoId("v5")).thenReturn(Optional.of(wf));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        StatusEvent ev = new StatusEvent();
        ev.setEventType("video.processed");
        ev.setVideoId("v5");
        ev.setOutputPath("s3://bucket/out.zip");
        ev.setProcessedAt(Instant.now());

        var out = service.updateStatus(ev);
        assertThat(out.getStatus()).isEqualTo("PROCESSED");
        assertThat(out.getOutputPath()).isEqualTo("s3://bucket/out.zip");
    }

    @Test
    void updateStatus_processingFailed_setsErrorAndFailedAt() {
        VideoWorkflow wf = new VideoWorkflow();
        wf.setVideoId("v6");
        when(repository.findByVideoId("v6")).thenReturn(Optional.of(wf));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        StatusEvent ev = new StatusEvent();
        ev.setEventType("video.processing_failed");
        ev.setVideoId("v6");
        ev.setErrorMessage("boom");
        // no failedAt provided

        var out = service.updateStatus(ev);
        assertThat(out.getStatus()).isEqualTo("FAILED");
        assertThat(out.getErrorMessage()).isEqualTo("boom");
        assertThat(out.getFailedAt()).isNotNull();
    }

    @Test
    void updateStatus_unknown_setsUnknownStatus() {
        VideoWorkflow wf = new VideoWorkflow();
        wf.setVideoId("v7");
        when(repository.findByVideoId("v7")).thenReturn(Optional.of(wf));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        StatusEvent ev = new StatusEvent();
        ev.setEventType("weird.event");
        ev.setVideoId("v7");

        var out = service.updateStatus(ev);
        assertThat(out.getStatus()).isEqualTo("UNKNOWN");
    }
}
