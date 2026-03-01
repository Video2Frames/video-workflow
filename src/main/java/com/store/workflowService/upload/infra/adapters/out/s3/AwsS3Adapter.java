package com.store.workflowService.upload.infra.adapters.out.s3;

import com.store.workflowService.upload.domain.model.Video;
import com.store.workflowService.upload.domain.model.VideoUploadResult;
import com.store.workflowService.upload.domain.repository.VideoStoragePort;
import com.store.workflowService.upload.infra.config.UploadProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.InputStream;

@Component
public class AwsS3Adapter implements VideoStoragePort {

    private final S3Client s3Client;
    private final UploadProperties uploadProperties;

    public AwsS3Adapter(S3Client s3Client, UploadProperties uploadProperties) {
        this.s3Client = s3Client;
        this.uploadProperties = uploadProperties;
    }

    @Override
    public VideoUploadResult upload(Video video, InputStream content, long contentLength) {

        // Build filename as id + extension (keep only extension from original filename)
        String originalFileName = video.getFileName();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        }
        String fileName = video.getId() + extension;

        String key = uploadProperties.getKeyPrefix()
                + "/"
                + video.getUserId()
                + "/"
                + fileName;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(uploadProperties.getBucket())
                .key(key)
                .build();

        PutObjectResponse response =
                s3Client.putObject(request, RequestBody.fromInputStream(content, contentLength));

        return new VideoUploadResult(
                uploadProperties.getBucket(),
                key,
                response.eTag()
        );
    }
}