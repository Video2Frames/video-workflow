package com.store.workflowService.consultation.infrastructure.adapters.in.controller;

import com.store.workflowService.consultation.application.usecases.ConsultationUseCase;
import com.store.workflowService.consultation.application.usecases.dto.DownloadResult;
import com.store.workflowService.consultation.application.usecases.dto.VideoDto;
import com.store.workflowService.consultation.infrastructure.adapters.in.dto.VideoResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/consultation")
public class ConsultationController {

    private final ConsultationUseCase useCase;

    public ConsultationController(ConsultationUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/videos")
    public ResponseEntity<List<VideoResponse>> listVideosByUser(@RequestParam(name = "user_id") String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        List<VideoDto> dtos = useCase.listByUser(userId);
        List<VideoResponse> response = dtos.stream().map(d -> new VideoResponse(
                d.getVideoId(), d.getUserId(), d.getUploadPath(), d.getOutputPath(), d.getStatus(), d.getUploadedAt(), d.getProcessedAt()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadVideo(
            @RequestParam("video_id") String videoId,
            @RequestParam(name = "user_id") String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        DownloadResult res = useCase.downloadOutput(userId, videoId);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + res.getFileName() + "\"");

        // If the file is a zip (we created it for a folder), set proper media type
        MediaType contentType = res.getMediaType();
        if (res.getFileName() != null && res.getFileName().toLowerCase().endsWith(".zip")) {
            contentType = MediaType.parseMediaType("application/zip");
        }

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(contentType)
                .body(res.getResource());
    }
}
