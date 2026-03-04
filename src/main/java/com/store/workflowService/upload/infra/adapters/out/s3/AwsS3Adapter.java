package com.store.workflowService.upload.infra.adapters.out.s3;

import com.store.workflowService.upload.domain.model.Video;
import com.store.workflowService.upload.domain.model.VideoUploadResult;
import com.store.workflowService.upload.domain.repository.VideoStoragePort;
import com.store.workflowService.upload.infra.config.UploadProperties;
import com.store.workflowService.utils.exception.VideoUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.InputStream;

@Component
public class AwsS3Adapter implements VideoStoragePort {

    private static final Logger log = LoggerFactory.getLogger(AwsS3Adapter.class);

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

        try {
            log.info("Uploading to S3 bucket='{}' key='{}' contentLength={}", uploadProperties.getBucket(), key, contentLength);

            PutObjectResponse response = s3Client.putObject(request, RequestBody.fromInputStream(content, contentLength));

            log.info("S3 putObject succeeded: eTag={}", response.eTag());

            return new VideoUploadResult(
                    uploadProperties.getBucket(),
                    key,
                    response.eTag()
            );
        } catch (Exception e) {
            log.error("Failed to upload to S3 bucket='{}' key='{}'. Error: {}", uploadProperties.getBucket(), key, e.toString(), e);
            throw new VideoUploadException("Failed to upload file to S3", e);
        }
    }
}