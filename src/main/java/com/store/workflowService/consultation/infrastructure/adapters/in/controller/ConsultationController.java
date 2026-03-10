package com.store.workflowService.consultation.infrastructure.adapters.in.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.workflowService.consultation.application.usecases.ConsultationUseCase;
import com.store.workflowService.consultation.application.usecases.dto.DownloadResult;
import com.store.workflowService.consultation.application.usecases.dto.VideoDto;
import com.store.workflowService.consultation.infrastructure.adapters.in.dto.VideoResponse;
import com.store.workflowService.update.infra.adapters.out.client.UserAuthClient;
import com.store.workflowService.update.infra.adapters.out.client.dto.UserAuthResponseDTO;
import com.store.workflowService.utils.exception.VideoUploadException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/consultation")
public class ConsultationController {

    private final ConsultationUseCase useCase;
    private final UserAuthClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ConsultationController(ConsultationUseCase useCase, UserAuthClient client) {
        this.useCase = useCase;
        this.client = client;
    }

    @GetMapping("/videos")
    public ResponseEntity<List<VideoResponse>> listVideosByUser(@RequestHeader(value = "Authorization") String bearerToken) {

        if (bearerToken == null || bearerToken.isBlank()) {
            throw new VideoUploadException("Authorization header is required");
        }

        String header = bearerToken.trim();
        String lower = header.toLowerCase();
        if (lower.startsWith("authorization:")) {
            header = header.substring("authorization:".length()).trim();
            lower = header.toLowerCase();
        }

        if (lower.startsWith("bearer ")) {
            String raw = header.substring(7).trim();
            header = "Bearer " + raw;
        } else {
            header = "Bearer " + header;
        }

        String userId;
        try {
            String raw = client.getUserInfo(header);
            if (raw == null || raw.isBlank()) {
                throw new VideoUploadException("Empty response from user auth service");
            }

            String json = raw.trim();
            if (json.startsWith("\"") && json.endsWith("\"")) {
                json = objectMapper.readValue(json, String.class);
            }

            UserAuthResponseDTO userInfo = objectMapper.readValue(json, UserAuthResponseDTO.class);
            if (userInfo == null || userInfo.getId() == null || userInfo.getId().isBlank()) {
                throw new VideoUploadException("Unable to resolve user from authorization token");
            }
            userId = userInfo.getId();
        } catch (IOException e) {
            throw new VideoUploadException("Failed to parse user info response", e);
        } catch (Exception e) {
            throw new VideoUploadException("Failed to resolve user from authorization service", e);
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
            @RequestHeader(value = "Authorization") String bearerToken) {

        String header = bearerToken.trim();
        String lower = header.toLowerCase();
        if (lower.startsWith("authorization:")) {
            header = header.substring("authorization:".length()).trim();
            lower = header.toLowerCase();
        }

        if (lower.startsWith("bearer ")) {
            String raw = header.substring(7).trim();
            header = "Bearer " + raw;
        } else {
            header = "Bearer " + header;
        }

        String userId;
        try {
            String raw = client.getUserInfo(header);
            if (raw == null || raw.isBlank()) {
                throw new VideoUploadException("Empty response from user auth service");
            }

            String json = raw.trim();
            if (json.startsWith("\"") && json.endsWith("\"")) {
                json = objectMapper.readValue(json, String.class);
            }

            UserAuthResponseDTO userInfo = objectMapper.readValue(json, UserAuthResponseDTO.class);
            if (userInfo == null || userInfo.getId() == null || userInfo.getId().isBlank()) {
                throw new VideoUploadException("Unable to resolve user from authorization token");
            }
            userId = userInfo.getId();
        } catch (IOException e) {
            throw new VideoUploadException("Failed to parse user info response", e);
        } catch (Exception e) {
            throw new VideoUploadException("Failed to resolve user from authorization service", e);
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
