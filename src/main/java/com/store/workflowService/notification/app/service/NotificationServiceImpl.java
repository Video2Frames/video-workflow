package com.store.workflowService.notification.app.service;

import com.store.workflowService.notification.app.usecases.NotificationUseCase;
import com.store.workflowService.notification.infra.adapters.out.ses.AwsSesAdapter;
import com.store.workflowService.update.app.usecases.dto.StatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final AwsSesAdapter sesAdapter;
    private final String fallbackRecipient;

    public NotificationServiceImpl(AwsSesAdapter sesAdapter,
                                   @Value("${aws.ses.from-email") String fallbackRecipient) {
        this.sesAdapter = sesAdapter;
        this.fallbackRecipient = fallbackRecipient;
    }

    @Override
    public void handle(StatusEvent event) {
        try {
            String eventType = event.getEventType();
            if (eventType == null) {
                log.warn("Notification event without eventType, skipping: {}", event);
                return;
            }

            String recipient = event.getUserEmail() != null && !event.getUserEmail().isBlank()
                    ? event.getUserEmail()
                    : fallbackRecipient;

            if ("video.processed".equals(eventType)) {
                sesAdapter.sendProcessedEmail(recipient, event);
            } else if ("video.processing_failed".equals(eventType)) {
                sesAdapter.sendFailedEmail(recipient, event);
            } else {
                log.debug("Unhandled notification event type={}", eventType);
            }

        } catch (Exception e) {
            log.error("Error handling notification event: {}", e.getMessage(), e);
        }
    }
}
