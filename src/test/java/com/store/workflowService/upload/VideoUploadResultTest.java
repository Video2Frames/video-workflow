package com.store.workflowService.upload;

import com.store.workflowService.upload.domain.model.VideoUploadResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VideoUploadResultTest {

    @Test
    void getters_work() {
        VideoUploadResult r = new VideoUploadResult("bucket","key","etag");
        assertThat(r.getBucket()).isEqualTo("bucket");
        assertThat(r.getKey()).isEqualTo("key");
        assertThat(r.getEtag()).isEqualTo("etag");
    }
}

