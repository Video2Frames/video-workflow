package com.store.workflowService.update.app.usecases;

import com.store.workflowService.update.domain.model.VideoWorkflow;
import com.store.workflowService.update.app.usecases.dto.StatusEvent;

public interface UpdateStatusUseCase {
    VideoWorkflow updateStatus(StatusEvent event);
}
