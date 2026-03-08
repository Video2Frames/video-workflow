package com.store.workflowService.notification.app.usecases;

import com.store.workflowService.update.app.usecases.dto.StatusEvent;

public interface NotificationUseCase {
    void handle(StatusEvent event);
}

