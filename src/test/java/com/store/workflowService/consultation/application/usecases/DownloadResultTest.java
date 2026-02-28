package com.store.workflowService.consultation.application.usecases;

import com.store.workflowService.consultation.application.usecases.dto.DownloadResult;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class DownloadResultTest {

    @Test
    void downloadResult_getters() {
        InputStreamResource r = new InputStreamResource(new ByteArrayInputStream(new byte[0]));
        DownloadResult dr = new DownloadResult(r, MediaType.APPLICATION_OCTET_STREAM, "file.zip");
        assertThat(dr.getFileName()).isEqualTo("file.zip");
        assertThat(dr.getMediaType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
        assertThat(dr.getResource()).isSameAs(r);
    }
}

