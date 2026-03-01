package com.store.workflowService.upload.app.service;

import com.store.workflowService.upload.app.usecases.UploadVideoUseCase;
import com.store.workflowService.upload.domain.model.Video;
import com.store.workflowService.upload.domain.model.VideoUploadResult;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class UploadVideoService {

    private final UploadVideoUseCase useCase;

    public UploadVideoService(UploadVideoUseCase useCase) {
        this.useCase = useCase;
    }

    public VideoUploadResult upload(String userId, String fileName, InputStream content, long contentLength) {

        Video video = new Video(userId, fileName);

        return useCase.execute(video, content, contentLength);
    }
}