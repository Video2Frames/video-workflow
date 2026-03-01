package com.store.workflowService.consultation.application.usecases;

import com.store.workflowService.consultation.application.usecases.ConsultationUseCase;
import com.store.workflowService.consultation.application.usecases.dto.DownloadResult;
import com.store.workflowService.consultation.application.usecases.dto.VideoDto;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ConsultationUseCaseTest {

    @Test
    void interface_canBeMocked_and_returnsList() {
        ConsultationUseCase uc = Mockito.mock(ConsultationUseCase.class);
        VideoDto dto = new VideoDto("v","u","up","op","S", null, null);
        when(uc.listByUser("u")).thenReturn(List.of(dto));

        var res = uc.listByUser("u");
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getVideoId()).isEqualTo("v");
    }

    @Test
    void interface_downloadOutput_canBeMocked() {
        ConsultationUseCase uc = Mockito.mock(ConsultationUseCase.class);
        DownloadResult dr = Mockito.mock(DownloadResult.class);
        when(uc.downloadOutput("u","v")).thenReturn(dr);

        var out = uc.downloadOutput("u","v");
        assertThat(out).isSameAs(dr);
    }
}

