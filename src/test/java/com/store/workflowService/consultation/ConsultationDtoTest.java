package com.store.workflowService.consultation;

import com.store.workflowService.consultation.application.usecases.dto.DownloadResult;
import com.store.workflowService.consultation.application.usecases.dto.VideoDto;
import com.store.workflowService.consultation.infrastructure.adapters.in.dto.VideoResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ConsultationDtoTest {

    @Test
    void downloadResult_getters() {
        InputStreamResource r = new InputStreamResource(new ByteArrayInputStream(new byte[0]));
        DownloadResult dr = new DownloadResult(r, MediaType.APPLICATION_OCTET_STREAM, "f.zip");
        assertThat(dr.getFileName()).isEqualTo("f.zip");
        assertThat(dr.getMediaType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
        assertThat(dr.getResource()).isSameAs(r);
    }

    @Test
    void videoDto_and_response_mapping() {
        Instant now = Instant.now();
        VideoDto dto = new VideoDto("vid","user","u","o","S", now, now);
        VideoResponse resp = new VideoResponse(dto.getVideoId(), dto.getUserId(), dto.getUploadPath(), dto.getOutputPath(), dto.getStatus(), dto.getUploadedAt(), dto.getProcessedAt());
        assertThat(resp.getVideoId()).isEqualTo("vid");
        assertThat(resp.getOutputPath()).isEqualTo("o");
    }
}

