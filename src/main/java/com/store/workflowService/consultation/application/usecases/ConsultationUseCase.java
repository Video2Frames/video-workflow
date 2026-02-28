package com.store.workflowService.consultation.application.usecases;

import com.store.workflowService.consultation.application.usecases.dto.DownloadResult;
import com.store.workflowService.consultation.application.usecases.dto.VideoDto;

import java.util.List;

public interface ConsultationUseCase {
    List<VideoDto> listByUser(String userId);

    DownloadResult downloadOutput(String userId, String videoId);
}

