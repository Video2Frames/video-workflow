package com.store.workflowService.notification.app.service;

import com.store.workflowService.notification.infra.adapters.out.ses.AwsSesAdapter;
import com.store.workflowService.update.app.usecases.dto.StatusEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private AwsSesAdapter sesAdapter;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(sesAdapter, "fallback@example.com");
    }

    @Test
    void handle_shouldSkipWhenEventTypeNull() {
        StatusEvent event = new StatusEvent();
        event.setEventType(null);

        service.handle(event);

        verifyNoInteractions(sesAdapter);
    }

    @Test
    void handle_shouldCallProcessedEmailWhenEventTypeProcessed_andUserEmailPresent() {
        StatusEvent event = new StatusEvent();
        event.setEventType("video.processed");
        event.setUserEmail("user@example.com");
        event.setVideoId("vid-1");

        service.handle(event);

        verify(sesAdapter, times(1)).sendProcessedEmail("user@example.com", event);
        verifyNoMoreInteractions(sesAdapter);
    }

    @Test
    void handle_shouldUseFallbackWhenUserEmailMissingAndFailEvent() {
        StatusEvent event = new StatusEvent();
        event.setEventType("video.processing_failed");
        event.setVideoId("vid-2");

        service.handle(event);

        verify(sesAdapter, times(1)).sendFailedEmail("fallback@example.com", event);
    }

    @Test
    void handle_shouldIgnoreUnknownEventType() {
        StatusEvent event = new StatusEvent();
        event.setEventType("something.else");
        event.setUserEmail("u@example.com");

        service.handle(event);

        verifyNoInteractions(sesAdapter);
    }
}
