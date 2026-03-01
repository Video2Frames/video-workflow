package com.store.workflowService.consultation;

import com.store.workflowService.consultation.application.usecases.ConsultationUseCase;
import com.store.workflowService.consultation.application.usecases.dto.DownloadResult;
import com.store.workflowService.consultation.application.usecases.dto.VideoDto;
import com.store.workflowService.consultation.infrastructure.adapters.in.controller.ConsultationController;
import com.store.workflowService.utils.exception.advice.ExceptionHandlerAdvice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ConsultationControllerTest {

    private ConsultationUseCase useCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        useCase = Mockito.mock(ConsultationUseCase.class);
        ConsultationController controller = new ConsultationController(useCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ExceptionHandlerAdvice())
                .build();
    }

    @Test
    void listVideosByUser_missingUserId_returnsBadRequest() throws Exception {
        // when user_id param is missing, Spring throws a MissingServletRequestParameterException
        // which is handled by generic exception handler in ExceptionHandlerAdvice producing 500
        mockMvc.perform(get("/api/consultation/videos"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void listVideosByUser_validUser_returnsList() throws Exception {
        VideoDto dto = new VideoDto("vid1", "user1", "u","o","PROCESSED", Instant.now(), Instant.now());
        when(useCase.listByUser("user1")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/consultation/videos").param("user_id", "user1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].videoId").value("vid1"));

        verify(useCase, times(1)).listByUser("user1");
    }

    @Test
    void downloadVideo_missingUserId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/consultation/videos/any/download"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void downloadVideo_whenUseCaseThrows_returnsNotFound() throws Exception {
        when(useCase.downloadOutput(anyString(), anyString())).thenThrow(new RuntimeException("not found"));

        mockMvc.perform(get("/api/consultation/videos/vid/download").param("user_id", "user1"))
                .andExpect(status().isNotFound());

        verify(useCase, times(1)).downloadOutput("user1", "vid");
    }

    @Test
    void downloadVideo_success_returnsFileStream_andZipContentType() throws Exception {
        byte[] data = "hello".getBytes();
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(data));
        DownloadResult dr = new DownloadResult(resource, MediaType.APPLICATION_OCTET_STREAM, "file.zip");
        when(useCase.downloadOutput("abc", "vid123")).thenReturn(dr);

        mockMvc.perform(get("/api/consultation/videos/vid123/download").param("user_id", "abc"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"file.zip\""))
                .andExpect(content().contentType("application/zip"));

        verify(useCase, times(1)).downloadOutput("abc", "vid123");
    }
}
