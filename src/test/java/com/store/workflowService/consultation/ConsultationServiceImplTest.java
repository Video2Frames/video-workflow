package com.store.workflowService.consultation;

import com.store.workflowService.consultation.application.service.ConsultationServiceImpl;
import com.store.workflowService.update.domain.model.VideoWorkflow;
import com.store.workflowService.update.domain.repository.VideoWorkflowRepository;
import com.store.workflowService.upload.infra.config.UploadProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConsultationServiceImplTest {

    private VideoWorkflowRepository repository;
    private S3Client s3Client;
    private UploadProperties uploadProperties;
    private ConsultationServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(VideoWorkflowRepository.class);
        s3Client = Mockito.mock(S3Client.class);
        uploadProperties = Mockito.mock(UploadProperties.class);
        when(uploadProperties.getBucket()).thenReturn("video-bucket");
        when(uploadProperties.getKeyPrefix()).thenReturn("prefix");
        service = new ConsultationServiceImpl(repository, s3Client, uploadProperties);
    }

    @Test
    void listByUser_mapsEntitiesToDto() {
        VideoWorkflow wf = new VideoWorkflow();
        wf.setVideoId("vid1");
        wf.setUserId("u1");
        wf.setUploadPath("/u");
        wf.setOutputPath("/o");
        wf.setStatus("PROCESSED");
        wf.setUploadedAt(Instant.now());
        wf.setProcessedAt(Instant.now());

        when(repository.findByUserId("u1")).thenReturn(List.of(wf));

        var dtos = service.listByUser("u1");
        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getVideoId()).isEqualTo("vid1");
    }

    @Test
    void downloadOutput_singleObject_returnsDownloadResult() throws Exception {
        VideoWorkflow wf = new VideoWorkflow();
        wf.setVideoId("v1");
        wf.setUserId("abc");
        wf.setOutputPath("s3://video-bucket/path/to/file-name.zip");

        when(repository.findByVideoId("v1")).thenReturn(java.util.Optional.of(wf));

        byte[] bytes = "hi".getBytes();
        ResponseInputStream<GetObjectResponse> ris = new ResponseInputStream<>(GetObjectResponse.builder().contentLength((long) bytes.length).build(), new ByteArrayInputStream(bytes));
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(ris);

        var res = service.downloadOutput("abc", "v1");
        assertThat(res).isNotNull();
        assertThat(res.getFileName()).isEqualTo("file-name.zip");
        assertThat(res.getMediaType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
        // read resource stream to ensure it is available
        var in = res.getResource().getInputStream();
        byte[] read = in.readAllBytes();
        assertThat(read).hasSize(bytes.length);
    }

    @Test
    void downloadOutput_userNotOwner_throws() {
        VideoWorkflow wf = new VideoWorkflow();
        wf.setVideoId("v1");
        wf.setUserId("other");
        wf.setOutputPath("s3://video-bucket/path/to/file.zip");
        when(repository.findByVideoId("v1")).thenReturn(java.util.Optional.of(wf));

        assertThrows(RuntimeException.class, () -> service.downloadOutput("abc", "v1"));
    }

    @Test
    void downloadOutput_noSuchKey_and_noContents_throws() {
        VideoWorkflow wf = new VideoWorkflow();
        wf.setVideoId("v2");
        wf.setUserId("abc");
        wf.setOutputPath("some/nonexistent/key");
        when(repository.findByVideoId("v2")).thenReturn(java.util.Optional.of(wf));

        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(NoSuchKeyException.builder().message("no").build());
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(ListObjectsV2Response.builder().contents(List.of()).build());

        assertThrows(RuntimeException.class, () -> service.downloadOutput("abc", "v2"));
    }

    @Test
    void downloadOutput_missingVideo_throws() {
        when(repository.findByVideoId("missing")).thenReturn(java.util.Optional.empty());
        assertThrows(RuntimeException.class, () -> service.downloadOutput("abc", "missing"));
    }
}
