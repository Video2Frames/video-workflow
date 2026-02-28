package com.store.workflowService.update;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.workflowService.update.app.usecases.UpdateStatusUseCase;
import com.store.workflowService.update.infra.adapters.in.messaging.SqsStatusListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SqsStatusListenerTest {

    private SqsClient sqsClient;
    private UpdateStatusUseCase updateStatusUseCase;
    private ObjectMapper objectMapper;
    private SqsStatusListener listener;

    @BeforeEach
    void setUp() {
        sqsClient = Mockito.mock(SqsClient.class);
        updateStatusUseCase = Mockito.mock(UpdateStatusUseCase.class);
        objectMapper = new ObjectMapper();
        listener = new SqsStatusListener(sqsClient, updateStatusUseCase, objectMapper, "http://queue-url");
    }

    @Test
    void pollStatusQueue_noMessages_noException() {
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(ReceiveMessageResponse.builder().messages(List.of()).build());
        listener.pollStatusQueue();
        verify(sqsClient, times(1)).receiveMessage(any(ReceiveMessageRequest.class));
        verifyNoInteractions(updateStatusUseCase);
    }
}
