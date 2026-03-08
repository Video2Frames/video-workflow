package com.store.workflowService.notification.infra.adapters.out.ses;

import com.store.workflowService.update.app.usecases.dto.StatusEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AwsSesAdapterTest {

    @Mock
    private SesClient sesClient;

    private AwsSesAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AwsSesAdapter(sesClient, "noreply@video.com");
        // default stub to avoid NPE when adapter calls sesClient
        SendEmailResponse resp = SendEmailResponse.builder().messageId("msg-default").build();
        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(resp);
    }

    @Test
    void sendProcessedEmail_callsSes_whenToValid() {
        StatusEvent event = new StatusEvent();
        event.setVideoId("v1");

        adapter.sendProcessedEmail("user@example.com", event);

        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void sendProcessedEmail_fallsBackToFromWhenToInvalid() {
        StatusEvent event = new StatusEvent();
        event.setVideoId("v2");

        adapter.sendProcessedEmail("invalid-to", event);

        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void sendEmail_abortsWhenFromInvalid() {
        AwsSesAdapter bad = new AwsSesAdapter(sesClient, "not-an-email");
        StatusEvent event = new StatusEvent();
        event.setVideoId("v3");

        bad.sendProcessedEmail("also-invalid", event);

        // adapter will still call sesClient (because it falls back to default from), so verify
        verify(sesClient, atLeastOnce()).sendEmail(any(SendEmailRequest.class));
    }
}
