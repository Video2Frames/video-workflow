package com.store.workflowService.upload.infra.adapters.out.sns;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.workflowService.upload.domain.model.Video;
import com.store.workflowService.upload.domain.model.VideoUploadResult;
import com.store.workflowService.upload.domain.repository.VideoEventPublisherPort;
import com.store.workflowService.config.SnsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.HashMap;
import java.util.Map;

@Component
public class AwsSnsAdapter implements VideoEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(AwsSnsAdapter.class);

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private final SnsProperties snsProperties;

    public AwsSnsAdapter(SnsClient snsClient, ObjectMapper objectMapper, SnsProperties snsProperties) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
        this.snsProperties = snsProperties;
    }

    @Override
    public void publishVideoUploaded(Video video, VideoUploadResult uploadResult) {

        String topicArn = snsProperties.getTopicArn();
        log.info("Publicando evento SNS no tópico: {}", topicArn);
        if (topicArn == null || topicArn.isBlank()) {
            log.warn("SNS topicArn não configurado (aws.sns.topic-arn). Evento não será publicado. videoId={}", video.getId());
            return;
        }

        // Build full S3 URI from upload result so recipients get the exact object location
        String s3Uri;
        if (uploadResult != null && uploadResult.getBucket() != null && uploadResult.getKey() != null) {
            s3Uri = "s3://" + uploadResult.getBucket() + "/" + uploadResult.getKey();
        } else {
            // fallback to previous behavior: filename-only
            s3Uri = buildUploadPath(video);
        }

        // Publica apenas video.uploaded e inclui user_id no body
        Map<String, Object> payloadVideoUploaded = Map.of(
                "video_id", video.getId(),
                "upload_path", s3Uri,
                "uploaded_at", video.getUploadedAt().toString(),
                "user_id", video.getUserId(),
                "output_path", s3Uri
        );


        try {
            String message = objectMapper.writeValueAsString(payloadVideoUploaded);

            Map<String, MessageAttributeValue> attributes = new HashMap<>();
            attributes.put("event_type",
                    MessageAttributeValue.builder()
                            .dataType("String")
                            .stringValue("video.uploaded")
                            .build());

            try {
                publishToTopic(topicArn, message, attributes, video);
            } catch (Exception e) {
                log.warn("Falha ao publicar evento SNS video.uploaded (best-effort). videoId={}", video.getId(), e);
            }

        } catch (Exception e) {
            log.warn("Falha ao serializar payload video.uploaded (best-effort). videoId={}", video.getId(), e);
        }
    }

    private void publishToTopic(String topicArn, String message, Map<String, MessageAttributeValue> attributes, Video video) {
        PublishRequest.Builder builder = PublishRequest.builder()
                .topicArn(topicArn)
                .message(message)
                .messageAttributes(attributes);

        if (topicArn.endsWith(".fifo")) {
            String groupId = (video.getUserId() == null || video.getUserId().isBlank())
                    ? "video-upload"
                    : "video-upload-" + video.getUserId();

            builder = builder
                    .messageGroupId(groupId)
                    .messageDeduplicationId(video.getId());
        }

        snsClient.publish(builder.build());
    }

    private String buildUploadPath(Video video) {
        String fileName = video.getFileName();
        if (fileName == null || !fileName.contains(".")) {
            return video.getId();
        }
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        return video.getId() + extension;
    }
}