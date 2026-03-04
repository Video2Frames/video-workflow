package com.store.workflowService.upload.infra.adapters.in.controller;

import com.store.workflowService.upload.app.service.UploadVideoService;
import com.store.workflowService.upload.domain.model.VideoUploadResult;
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

    public VideoUploadController(UploadVideoService service) {
        this.service = service;
    }


    @PostMapping(value = "/videos", consumes = "multipart/form-data")
    public ResponseEntity<Void> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId
    ) {
        if (userId == null || userId.isBlank()) {
            throw new VideoUploadException("userId é obrigatório");
        }
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

        try (InputStream in = file.getInputStream()) {
            VideoUploadResult result = service.upload(
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