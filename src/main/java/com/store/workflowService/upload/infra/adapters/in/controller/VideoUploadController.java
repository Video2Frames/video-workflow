package com.store.workflowService.upload.infra.adapters.in.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.workflowService.update.infra.adapters.out.client.UserAuthClient;
import com.store.workflowService.upload.app.service.UploadVideoService;
import com.store.workflowService.update.infra.adapters.out.client.dto.UserAuthResponseDTO;
import com.store.workflowService.utils.exception.VideoUploadException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/upload")
public class VideoUploadController {

    private static final long MAX_UPLOAD_BYTES = 200L * 1024L * 1024L;

    private final UploadVideoService service;
    private final UserAuthClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VideoUploadController(UploadVideoService service, UserAuthClient client) {
        this.service = service;
        this.client = client;
    }


    @PostMapping(value = "/videos", consumes = "multipart/form-data")
    public ResponseEntity<Void> upload(
            @RequestParam(value = "file") MultipartFile file,
            @RequestHeader(value = "Authorization") String bearerToken) {
        if (file == null || file.isEmpty()) {
            throw new VideoUploadException("Arquivo é obrigatório");
        }
        if (file.getSize() <= 0) {
            throw new VideoUploadException("Arquivo inválido (tamanho 0)");
        }
        if (file.getSize() > MAX_UPLOAD_BYTES) {
            throw new VideoUploadException("Arquivo excede o limite permitido");
        }

        String safeFileName = sanitizeFileName(file.getOriginalFilename());

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

        try (InputStream in = file.getInputStream()) {
            service.upload(
                    userId.trim(),
                    safeFileName,
                    in,
                    file.getSize()
            );

            return ResponseEntity.ok().build();
        } catch (IOException e) {
            throw new VideoUploadException("Erro ao ler arquivo para upload", e);
        }
    }

    private static String sanitizeFileName(String original) {
        String name = (original == null || original.isBlank()) ? "video.bin" : original.trim();

        name = name.replace("\\", "_").replace("/", "_");
        name = name.replaceAll("\\s+", " ");

        int max = 180;
        if (name.length() > max) {
            name = name.substring(name.length() - max);
        }

        return name;
    }
}