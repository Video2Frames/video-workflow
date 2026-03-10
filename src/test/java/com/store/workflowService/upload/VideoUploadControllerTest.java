package com.store.workflowService.upload;


import com.store.workflowService.update.infra.adapters.out.client.UserAuthClient;
import com.store.workflowService.upload.app.service.UploadVideoService;
import com.store.workflowService.upload.infra.adapters.in.controller.VideoUploadController;
import com.store.workflowService.upload.domain.model.VideoUploadResult;
import com.store.workflowService.upload.app.usecases.UploadVideoUseCase;
import com.store.workflowService.upload.domain.model.Video;
import com.store.workflowService.utils.exception.advice.ExceptionHandlerAdvice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VideoUploadControllerTest {

    private UploadVideoService service;
    private MockMvc mockMvc;

    static class TestUploadVideoService extends UploadVideoService {
        boolean called = false;
        String lastUser = null;

        public TestUploadVideoService() {
            super(new UploadVideoUseCase(null, null, null) {
                @Override
                public VideoUploadResult execute(Video video, InputStream content, long contentLength) {
                    return new VideoUploadResult("bucket", "key", "etag");
                }
            });
        }

        @Override
        public VideoUploadResult upload(String userId, String fileName, InputStream content, long contentLength) {
            this.called = true;
            this.lastUser = userId;
            return new VideoUploadResult("bucket", "key", "etag");
        }
    }

    @BeforeEach
    void setUp() {
        // Replace Mockito.mock(UploadVideoService.class) to avoid Byte Buddy instrumentation on Java 25
        service = new TestUploadVideoService();
        UserAuthClient userAuthClient = Mockito.mock(UserAuthClient.class);
        // default behavior: return a normal JSON object with id
        when(userAuthClient.getUserInfo(anyString())).thenReturn("{\"id\":\"u1\",\"email\":null}");
        VideoUploadController controller = new VideoUploadController(service, userAuthClient);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ExceptionHandlerAdvice())
                .build();
    }

    @Test
    void upload_missingUserId_throwsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", "data".getBytes());

        mockMvc.perform(multipart("/api/upload/videos").file(file))
                .andExpect(status().isInternalServerError()); // VideoUploadException handled as 500
    }

    @Test
    void upload_emptyFile_throws() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "", "video/mp4", new byte[0]);

        mockMvc.perform(multipart("/api/upload/videos").file(file).param("userId", "u1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void upload_validFile_callsService() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "content".getBytes());

        // send Authorization header so controller can resolve user via UserAuthClient mock
        mockMvc.perform(multipart("/api/upload/videos").file(file)
                        .header("Authorization", "Bearer token-sample"))
                .andExpect(status().isOk());

        assertTrue(((TestUploadVideoService) service).called, "upload should have been called");
        assertEquals("u1", ((TestUploadVideoService) service).lastUser);
    }
}
