package com.store.workflowService.update.infra.adapters.in.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.workflowService.update.app.usecases.UpdateStatusUseCase;
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
public class SqsStatusListener {

    private static final Logger log = LoggerFactory.getLogger(SqsStatusListener.class);

    private final SqsClient sqsClient;
    private final UpdateStatusUseCase updateStatusUseCase;
    private final ObjectMapper objectMapper;
    private final String statusQueueUrl;

    public SqsStatusListener(SqsClient sqsClient,
                             UpdateStatusUseCase updateStatusUseCase,
                             ObjectMapper objectMapper,
                             @Value("${aws.sqs.status-queue-url}") String statusQueueUrl) {
        log.info("Processando Status de video - SQS Status Listener iniciado com queueUrl={}", statusQueueUrl);
        this.sqsClient = sqsClient;
        this.updateStatusUseCase = updateStatusUseCase;
        this.objectMapper = objectMapper;
        this.statusQueueUrl = statusQueueUrl;
    }

    @Scheduled(fixedDelayString = "${aws.sqs.poll-delay-ms:5000}")
    public void pollStatusQueue() {
        try {
            ReceiveMessageRequest req = ReceiveMessageRequest.builder()
                    .queueUrl(statusQueueUrl)
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
                    log.info("Received status message id={} body={}", m.messageId(), m.body());

                    JsonNode envelope = objectMapper.readTree(m.body());

                    String messageStr;
                    if (envelope.has("Message") && envelope.get("Message").isTextual()) {
                        messageStr = envelope.get("Message").asText();
                    } else {
                        messageStr = m.body();
                    }

                    JsonNode payload = objectMapper.readTree(messageStr);

                    // extrair event_type priorizando messageAttributes do SQS, depois envelope SNS, depois payload
                    String eventType = null;
                    if (m.messageAttributes() != null && m.messageAttributes().containsKey("event_type")) {
                        try {
                            eventType = m.messageAttributes().get("event_type").stringValue();
                        } catch (Exception ex) {
                            log.debug("Não conseguiu ler event_type de m.messageAttributes(): {}", ex.getMessage());
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

                    updateStatusUseCase.updateStatus(event);


                    sqsClient.deleteMessage(DeleteMessageRequest.builder()
                            .queueUrl(statusQueueUrl)
                            .receiptHandle(m.receiptHandle())
                            .build());
                } catch (Exception e) {
                    log.error("Error processing status message id={}: {}", m.messageId(), e.getMessage(), e);
                }
            }

        } catch (Exception e) {
            log.error("Error polling status queue: {}", e.getMessage(), e);
        }
    }
}
