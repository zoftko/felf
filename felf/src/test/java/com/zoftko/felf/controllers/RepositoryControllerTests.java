package com.zoftko.felf.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.zoftko.felf.db.dao.AnalysisRepository;
import com.zoftko.felf.db.entities.Analysis;
import com.zoftko.felf.db.entities.Installation;
import com.zoftko.felf.db.entities.Project;
import com.zoftko.felf.db.entities.Size;
import com.zoftko.felf.models.ProjectData;
import com.zoftko.felf.models.Repository;
import com.zoftko.felf.models.RepositoryInstallation;
import com.zoftko.felf.services.FelfService;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = RepositoryController.class)
class RepositoryControllerTests extends BaseControllerTest {

    @MockBean
    FelfService felfService;

    @MockBean
    AnalysisRepository analysisRepository;

    static final int installId = 123;
    static final int senderId = 78;

    static final String owner = "zoftko";
    static final String repo = "zynth53";
    static final String fullName = owner + "/" + repo;
    static final String defaultBranch = "main";

    final String getTestUrl = "/" + fullName;
    final String tokenTestUrl = getTestUrl + "/token";

    static Project project;
    static Installation installation;
    static RepositoryInstallation repoInstall;
    static Repository repository;

    @BeforeAll
    static void setupAll() {
        installation = new Installation();
        installation.setId(installId);
        installation.setSender(senderId);

        project = new Project();
        project.setDefaultBranch(defaultBranch);

        repoInstall = new RepositoryInstallation(installId, Map.of("pull_requests", "write"), null);
        repository = new Repository(fullName, false, "main");
    }

    @Test
    void noProjectNoRepo() throws Exception {
        when(felfService.getProjectData(owner, repo))
            .thenReturn(
                new ProjectData(fullName, Optional.empty(), Optional.of(new Installation()), null, null)
            );

        assertThat(
            mockMvc.perform(get(getTestUrl).with(githubLogin(123))).andReturn().getModelAndView().getModel()
        )
            .containsEntry("isOwner", false)
            .containsEntry("hasPermissions", false)
            .containsEntry("projectPresent", false);
    }

    @Test
    void projectNoAnalysis() throws Exception {
        when(felfService.getProjectData(owner, repo))
            .thenReturn(
                new ProjectData(
                    fullName,
                    Optional.of(project),
                    Optional.of(installation),
                    repoInstall,
                    repository
                )
            );
        when(analysisRepository.getLastAnalysisByRef(project, "main")).thenReturn(Optional.empty());

        assertThat(
            mockMvc
                .perform(get(getTestUrl).with(githubLogin(senderId)))
                .andReturn()
                .getModelAndView()
                .getModel()
        )
            .containsEntry("demo", true)
            .containsEntry("projectPresent", true)
            .containsEntry("isOwner", true)
            .containsEntry("hasPermissions", true);

        assertThat(
            mockMvc.perform(get(getTestUrl).with(githubLogin(99))).andReturn().getModelAndView().getModel()
        )
            .containsEntry("isOwner", false);
    }

    @Test
    void projectWithAnalysis() throws Exception {
        when(felfService.getProjectData(owner, repo))
            .thenReturn(
                new ProjectData(
                    fullName,
                    Optional.of(project),
                    Optional.of(installation),
                    repoInstall,
                    repository
                )
            );

        var analysis = new Analysis();
        analysis.setSize(new Size());
        analysis.getSize().setText(1024L);
        analysis.getSize().setData(32L);
        analysis.getSize().setBss(64L);
        when(analysisRepository.getLastAnalysisByRef(project, "main")).thenReturn(Optional.of(analysis));

        assertThat(
            mockMvc.perform(get(getTestUrl).with(githubLogin(1))).andReturn().getModelAndView().getModel()
        )
            .containsEntry("demo", false);
    }

    @Test
    void createRepositoryTokenNoProject() throws Exception {
        when(felfService.getFreshProjectData(owner, repo))
            .thenReturn(
                new ProjectData(
                    fullName,
                    Optional.empty(),
                    Optional.of(installation),
                    repoInstall,
                    repository
                )
            );

        assertThat(
            mockMvc
                .perform(post(tokenTestUrl).with(githubLogin(senderId + 1)).with(csrf()))
                .andReturn()
                .getResponse()
                .getStatus()
        )
            .isEqualTo(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS.value());

        assertThat(
            mockMvc
                .perform(post(tokenTestUrl).with(githubLogin(senderId)).with(csrf()))
                .andReturn()
                .getModelAndView()
                .getModel()
        )
            .containsKey("token");

        var argument = ArgumentCaptor.forClass(Project.class);
        verify(felfService).storeProject(argument.capture());

        var storedProject = argument.getValue();
        assertThat(storedProject).isNotNull();
        assertThat(storedProject.getToken()).isNotNull();
        assertThat(storedProject.getFullName()).isEqualTo(fullName);
        assertThat(storedProject.getDefaultBranch()).isEqualTo(defaultBranch);
        assertThat(storedProject.getInstallation()).isEqualTo(installation);
    }

    @Test
    void createRepositoryTokenPrivateRepo() throws Exception {
        var privateRepository = new Repository(fullName, true, defaultBranch);
        when(felfService.getFreshProjectData(owner, repo))
            .thenReturn(
                new ProjectData(
                    fullName,
                    Optional.empty(),
                    Optional.of(installation),
                    repoInstall,
                    privateRepository
                )
            );

        assertThat(
            mockMvc
                .perform(post(tokenTestUrl).with(githubLogin(senderId)).with(csrf()))
                .andReturn()
                .getResponse()
                .getStatus()
        )
            .isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}
