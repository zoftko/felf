package com.zoftko.felf.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zoftko.felf.dao.AnalysisRepository;
import com.zoftko.felf.dao.ProjectRepository;
import com.zoftko.felf.entities.Analysis;
import com.zoftko.felf.entities.Project;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;

@ContextConfiguration(classes = AnalysisController.class)
class AnalysisControllerTests extends BaseControllerTest {

    @MockBean
    ProjectRepository projectRepository;

    @MockBean
    AnalysisRepository analysisRepository;

    @MockBean
    PasswordEncoder encoder;

    String dummyPayload =
        """
        {
            "ref": "main",
            "sha": "2e4270767bc75244a96de06dfca512887d4b3adc",
            "size": {
                "text": 940,
                "data": 2,
                "bss": 6
            }
        }
        """;

    String invalidPayload =
        """
        {
            "ref": "main",
            "sha": "2e4270767bc75244a96de06dfca512887d4b3adc",
            "size": {
                "text": -940,
                "data": 2,
                "bss": 6
            }
        }
        """;

    ResultActions postAnalysis(String authorization, String content) throws Exception {
        return this.mockMvc.perform(
                post(AnalysisController.MAPPING)
                    .header("Authorization", authorization)
                    .header(AnalysisController.HEADER_REPO, "zoftko/felf")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
            );
    }

    @Test
    void invalidHeader() throws Exception {
        postAnalysis("Basic kiwi:blue", dummyPayload).andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    void invalidRepo() throws Exception {
        when(projectRepository.findByFullName(any())).thenReturn(Optional.empty());
        postAnalysis("Bearer magicalmisterytour", dummyPayload)
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void invalidToken() throws Exception {
        when(projectRepository.findByFullName(any())).thenReturn(Optional.of(new Project()));
        when(encoder.matches(any(), any())).thenReturn(false);
        postAnalysis("Bearer correct_token123", dummyPayload)
            .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    void validToken() throws Exception {
        var project = new Project();
        var argument = ArgumentCaptor.forClass(Analysis.class);

        when(projectRepository.findByFullName(any())).thenReturn(Optional.of(project));
        when(encoder.matches(any(), any())).thenReturn(true);

        postAnalysis("Bearer correct_token123", dummyPayload).andExpect(status().isOk());
        verify(analysisRepository).save(argument.capture());

        var storedAnalysis = argument.getValue();
        assertThat(storedAnalysis.getCreatedAt())
            .isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        assertThat(storedAnalysis.getProject()).isEqualTo(project);
        assertThat(storedAnalysis.getSize().getText()).isEqualTo(940);
        assertThat(storedAnalysis.getSize().getData()).isEqualTo(2);
        assertThat(storedAnalysis.getSize().getBss()).isEqualTo(6);
    }

    @Test
    void invalidPayload() throws Exception {
        when(projectRepository.findByFullName(any())).thenReturn(Optional.of(new Project()));
        when(encoder.matches(any(), any())).thenReturn(true);

        postAnalysis("Bearer correct_token123", invalidPayload)
            .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }
}
