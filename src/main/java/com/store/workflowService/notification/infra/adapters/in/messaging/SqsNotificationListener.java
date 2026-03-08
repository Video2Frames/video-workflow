package com.store.workflowService.notification.infra.adapters.in.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.workflowService.notification.app.usecases.NotificationUseCase;
import com.store.workflowService.update.app.usecases.dto.StatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

@Component
public class SqsNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(SqsNotificationListener.class);

    private final SqsClient sqsClient;
    private final NotificationUseCase notificationUseCase;
    private final ObjectMapper objectMapper;
    private final String notificationsQueueUrl;
    private final boolean enabled;
    private final String testRecipient;

    public SqsNotificationListener(SqsClient sqsClient,
                                   NotificationUseCase notificationUseCase,
                                   ObjectMapper objectMapper,
                                   @Value("${aws.sqs.notifications-queue-url:}") String notificationsQueueUrl,
                                   @Value("${notification.test.recipient:}") String testRecipient) {
        this.sqsClient = sqsClient;
        this.notificationUseCase = notificationUseCase;
        this.objectMapper = objectMapper;
        this.notificationsQueueUrl = notificationsQueueUrl == null ? "" : notificationsQueueUrl.trim();
        this.testRecipient = testRecipient == null ? "" : testRecipient.trim();
        this.enabled = !this.notificationsQueueUrl.isBlank();

        if (enabled) {
            log.info("Notification SQS Listener started with queueUrl={}", this.notificationsQueueUrl);
        } else {
            log.warn("Notification SQS Listener is disabled because 'aws.sqs.notifications-queue-url' is not configured");
        }
    }

    @Scheduled(fixedDelayString = "${aws.sqs.poll-delay-ms:5000}")
    public void pollNotificationsQueue() {
        if (!enabled) {
            // listener disabled in this environment
            return;
        }

        try {
            ReceiveMessageRequest req = ReceiveMessageRequest.builder()
                    .queueUrl(notificationsQueueUrl)
                    .waitTimeSeconds(20)
                    .maxNumberOfMessages(5)
                    .messageAttributeNames("All")
                    .build();

            List<Message> messages = sqsClient.receiveMessage(req).messages();
            if (messages.isEmpty()) {
                return;
            }

            for (Message m : messages) {
                try {
                    log.info("Received notification message id={} body={}", m.messageId(), m.body());

                    JsonNode envelope = objectMapper.readTree(m.body());

                    String messageStr;
                    if (envelope.has("Message") && envelope.get("Message").isTextual()) {
                        messageStr = envelope.get("Message").asText();
                    } else {
                        messageStr = m.body();
                    }

                    JsonNode payload = objectMapper.readTree(messageStr);

                    // extract event_type prioritizing SQS message attributes, then SNS envelope, then payload
                    String eventType = null;
                    if (m.messageAttributes() != null && m.messageAttributes().containsKey("event_type")) {
                        try {
                            eventType = m.messageAttributes().get("event_type").stringValue();
                        } catch (Exception ex) {
                            log.debug("Unable to read event_type from messageAttributes: {}", ex.getMessage());
                        }
                    }

                    if (eventType == null && envelope.has("MessageAttributes") && envelope.get("MessageAttributes").has("event_type")) {
                        JsonNode attrNode = envelope.get("MessageAttributes").get("event_type");
                        if (attrNode.has("Value")) {
                            eventType = attrNode.get("Value").asText();
                        }
                    }

                    if (eventType == null && payload.has("event_type")) {
                        eventType = payload.get("event_type").asText();
                    }

                    StatusEvent event = objectMapper.treeToValue(payload, StatusEvent.class);
                    event.setEventType(eventType);

                    // If a test recipient is configured (local testing), override recipient; otherwise use event payload
                    if (!this.testRecipient.isBlank()) {
                        event.setUserEmail(this.testRecipient);
                        log.info("Overriding recipient email for testing: {}", event.getUserEmail());
                    }

                    notificationUseCase.handle(event);

                    sqsClient.deleteMessage(DeleteMessageRequest.builder()
                            .queueUrl(notificationsQueueUrl)
                            .receiptHandle(m.receiptHandle())
                            .build());

                } catch (Exception e) {
                    log.error("Error processing notification message id={}: {}", m.messageId(), e.getMessage(), e);
                }
            }

        } catch (Exception e) {
            log.error("Error polling notifications queue: {}", e.getMessage(), e);
        }
    }
}
