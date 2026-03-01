package com.store.workflowService.consultation.application.usecases;

import com.store.workflowService.consultation.application.usecases.dto.VideoDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class VideoDtoTest {

    @Test
    void gettersAndConstructor_workAsExpected() {
        Instant now = Instant.now();
        VideoDto dto = new VideoDto("vid1", "user1", "uploadPath", "outputPath", "PROCESSED", now, now);

        assertThat(dto.getVideoId()).isEqualTo("vid1");
        assertThat(dto.getUserId()).isEqualTo("user1");
        assertThat(dto.getUploadPath()).isEqualTo("uploadPath");
        assertThat(dto.getOutputPath()).isEqualTo("outputPath");
        assertThat(dto.getStatus()).isEqualTo("PROCESSED");
        assertThat(dto.getUploadedAt()).isEqualTo(now);
        assertThat(dto.getProcessedAt()).isEqualTo(now);
    }
}

