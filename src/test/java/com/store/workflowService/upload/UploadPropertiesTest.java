package com.store.workflowService.upload;

import com.store.workflowService.upload.infra.config.UploadProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UploadPropertiesTest {

    @Test
    void gettersAndSetters_work() {
        UploadProperties p = new UploadProperties();
        p.setBucket("b");
        p.setKeyPrefix("k");
        assertThat(p.getBucket()).isEqualTo("b");
        assertThat(p.getKeyPrefix()).isEqualTo("k");
    }
}

