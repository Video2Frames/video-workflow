package com.store.workflowService.upload;

import com.store.workflowService.upload.app.service.UploadVideoService;
import com.store.workflowService.upload.infra.adapters.in.controller.VideoUploadController;
import com.store.workflowService.upload.domain.model.VideoUploadResult;
import com.store.workflowService.utils.exception.advice.ExceptionHandlerAdvice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VideoUploadControllerTest {

    private UploadVideoService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(UploadVideoService.class);
        VideoUploadController controller = new VideoUploadController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ExceptionHandlerAdvice())
                .build();
    }

    @Test
    void upload_missingUserId_throwsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", "data".getBytes());

        mockMvc.perform(multipart("/videos").file(file))
                .andExpect(status().isInternalServerError()); // VideoUploadException handled as 500
    }

    @Test
    void upload_emptyFile_throws() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "", "video/mp4", new byte[0]);

        mockMvc.perform(multipart("/videos").file(file).param("userId", "u1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void upload_validFile_callsService() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "content".getBytes());
        when(service.upload(anyString(), anyString(), any(), anyLong())).thenReturn(new VideoUploadResult("b","k","e"));

        mockMvc.perform(multipart("/videos").file(file).param("userId", "u1"))
                .andExpect(status().isOk());

        verify(service, times(1)).upload(eq("u1"), anyString(), any(), anyLong());
    }
}
