package com.store.workflowService.update;

import com.store.workflowService.update.app.usecases.dto.StatusEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class StatusEventTest {

    @Test
    void gettersSetters_work() {
        StatusEvent e = new StatusEvent();
        e.setEventType("video.processed");
        e.setVideoId("v");
        e.setUploadPath("/u");
        e.setUploadedAt(Instant.now());
        e.setProcessingStartedAt(Instant.now());
        e.setOutputPath("/out");
        e.setProcessedAt(Instant.now());
        e.setErrorMessage("e");
        e.setFailedAt(Instant.now());
        e.setUserId("u1");

        assertThat(e.getEventType()).isEqualTo("video.processed");
        assertThat(e.getVideoId()).isEqualTo("v");
        assertThat(e.getOutputPath()).isEqualTo("/out");
    }
}

