package com.store.workflowService.update;

import com.store.workflowService.update.domain.model.VideoWorkflow;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VideoWorkflowTest {

    @Test
    void gettersAndSetters_work() {
        VideoWorkflow v = new VideoWorkflow();
        UUID id = UUID.randomUUID();
        v.setId(id);
        v.setUserId("u");
        v.setVideoId("vid");
        v.setUploadPath("/up");
        v.setOutputPath("/out");
        v.setStatus("S");
        v.setErrorMessage("e");
        v.setUploadedAt(Instant.now());
        v.setProcessedAt(Instant.now());

        assertThat(v.getId()).isEqualTo(id);
        assertThat(v.getUserId()).isEqualTo("u");
        assertThat(v.getVideoId()).isEqualTo("vid");
        assertThat(v.getUploadPath()).isEqualTo("/up");
        assertThat(v.getOutputPath()).isEqualTo("/out");
        assertThat(v.getStatus()).isEqualTo("S");
        assertThat(v.getErrorMessage()).isEqualTo("e");
    }

    @Test
    void prePersist_setsIdAndLastUpdatedAt() {
        VideoWorkflow v = new VideoWorkflow();
        v.setId(null);
        v.prePersist();
        assertThat(v.getId()).isNotNull();
        assertThat(v.getLastUpdatedAt()).isNotNull();
    }

    @Test
    void preUpdate_updatesLastUpdatedAt() throws InterruptedException {
        VideoWorkflow v = new VideoWorkflow();
        Instant then = Instant.parse("2020-01-01T00:00:00Z");
        v.setLastUpdatedAt(then);
        Thread.sleep(10);
        v.preUpdate();
        assertThat(v.getLastUpdatedAt()).isAfter(then);
    }
}
