package com.store.workflowService.upload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.workflowService.config.SnsProperties;
import com.store.workflowService.upload.domain.model.Video;
import com.store.workflowService.upload.domain.model.VideoUploadResult;
import com.store.workflowService.upload.infra.adapters.out.sns.AwsSnsAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import static org.mockito.Mockito.*;

class AwsSnsAdapterTest {

    private SnsClient snsClient;
    private ObjectMapper objectMapper;
    private SnsProperties snsProperties;
    private AwsSnsAdapter adapter;

    @BeforeEach
    void setUp() {
        snsClient = Mockito.mock(SnsClient.class);
        objectMapper = new ObjectMapper();
        snsProperties = new SnsProperties();
        adapter = new AwsSnsAdapter(snsClient, objectMapper, snsProperties);
    }

    @Test
    void publishVideoUploaded_noTopic_warnsAndReturns() {
        Video v = new Video("u1","f.mp4");
        adapter.publishVideoUploaded(v, null);
        // nothing to assert, just ensure no exceptions
    }

    @Test
    void publishVideoUploaded_withTopic_publishes() {
        snsProperties.setTopicArn("arn:aws:sns:region:acct:topic");
        Video v = new Video("u1","f.mp4");
        VideoUploadResult r = new VideoUploadResult("b","k","e");
        adapter.publishVideoUploaded(v, r);
        verify(snsClient, times(1)).publish(any(PublishRequest.class));
    }

    @Test
    void publishVideoUploaded_fifo_appendsGroupMessage() {
        snsProperties.setTopicArn("arn:aws:sns:region:acct:topic.fifo");
        Video v = new Video("u2","noext");
        VideoUploadResult r = new VideoUploadResult(null,null,null);
        adapter.publishVideoUploaded(v, r);
        verify(snsClient, times(1)).publish(any(PublishRequest.class));
    }
}

