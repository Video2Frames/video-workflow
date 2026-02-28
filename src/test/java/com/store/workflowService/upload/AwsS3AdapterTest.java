package com.store.workflowService.upload;

import com.store.workflowService.upload.domain.model.Video;
import com.store.workflowService.upload.domain.model.VideoUploadResult;
import com.store.workflowService.upload.infra.adapters.out.s3.AwsS3Adapter;
import com.store.workflowService.upload.infra.config.UploadProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AwsS3AdapterTest {

    private S3Client s3Client;
    private UploadProperties props;
    private AwsS3Adapter adapter;

    @BeforeEach
    void setUp() {
        s3Client = Mockito.mock(S3Client.class);
        props = new UploadProperties();
        props.setBucket("video-bucket");
        props.setKeyPrefix("prefix");
        adapter = new AwsS3Adapter(s3Client, props);
    }

    @Test
    void upload_buildsKeyAndReturnsResult() {
        Video v = new Video("u1","file.mp4");
        PutObjectResponse por = PutObjectResponse.builder().eTag("etag-1").build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(por);

        var res = adapter.upload(v, new ByteArrayInputStream(new byte[0]), 10L);
        assertThat(res.getBucket()).isEqualTo("video-bucket");
        assertThat(res.getEtag()).isEqualTo("etag-1");
        assertThat(res.getKey()).contains("u1");
    }
}

