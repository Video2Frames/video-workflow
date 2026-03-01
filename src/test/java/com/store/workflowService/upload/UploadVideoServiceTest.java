package com.store.workflowService.upload;

import com.store.workflowService.upload.app.service.UploadVideoService;
import com.store.workflowService.upload.app.usecases.UploadVideoUseCase;
import com.store.workflowService.upload.domain.model.Video;
import com.store.workflowService.upload.domain.model.VideoUploadResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class UploadVideoServiceTest {

    private UploadVideoUseCase useCase;
    private UploadVideoService service;

    @BeforeEach
    void setUp() {
        useCase = Mockito.mock(UploadVideoUseCase.class);
        service = new UploadVideoService(useCase);
    }

    @Test
    void upload_delegatesToUseCase() {
        when(useCase.execute(any(Video.class), any(InputStream.class), anyLong())).thenReturn(new VideoUploadResult("b","k","e"));
        var res = service.upload("u","file.mp4", new ByteArrayInputStream(new byte[0]), 10L);
        assertThat(res).isNotNull();
        assertThat(res.getBucket()).isEqualTo("b");
    }
}

