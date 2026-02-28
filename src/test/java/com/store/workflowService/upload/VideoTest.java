package com.store.workflowService.upload;

import com.store.workflowService.upload.domain.model.Video;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VideoTest {

    @Test
    void constructor_setsFields() {
        Video v = new Video("u1","file.mp4");
        assertThat(v.getUserId()).isEqualTo("u1");
        assertThat(v.getFileName()).isEqualTo("file.mp4");
        assertThat(v.getId()).isNotNull();
        assertThat(v.getUploadedAt()).isNotNull();
    }
}

