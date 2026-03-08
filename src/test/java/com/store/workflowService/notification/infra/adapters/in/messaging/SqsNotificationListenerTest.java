package com.store.workflowService.notification.infra.adapters.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.workflowService.notification.app.usecases.NotificationUseCase;
import com.store.workflowService.update.app.usecases.dto.StatusEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SqsNotificationListenerTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private NotificationUseCase notificationUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // We'll instantiate listener with a non-empty queueUrl so it's enabled
    private SqsNotificationListener listener;

    @BeforeEach
    void setUp() {
        listener = new SqsNotificationListener(sqsClient, notificationUseCase, objectMapper, "http://queue-url");
    }

    @Test
    void pollNotificationsQueue_parsesSnsEnvelopeAndDelegates() {
        // Inner payload contains only mapped fields (video_id). event_type must come from MessageAttributes
        String snsEnvelope = "{\"Message\": \"{\\\"video_id\\\":\\\"id-1\\\"}\", \"MessageAttributes\": {\"event_type\": {\"Value\": \"video.processed\"}}}";

        Message msg = Message.builder().messageId("m1").body(snsEnvelope).receiptHandle("rh").build();
        ReceiveMessageResponse resp = ReceiveMessageResponse.builder().messages(msg).build();
        when(sqsClient.receiveMessage(any(software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest.class))).thenReturn(resp);

        // call the poll method directly
        listener.pollNotificationsQueue();

        ArgumentCaptor<StatusEvent> captor = ArgumentCaptor.forClass(StatusEvent.class);
        verify(notificationUseCase, times(1)).handle(captor.capture());

        StatusEvent ev = captor.getValue();
        assertEquals("id-1", ev.getVideoId());
        assertEquals("video.processed", ev.getEventType());
        assertEquals("ponteskarenklp82@gmail.com", ev.getUserEmail());
    }
}
