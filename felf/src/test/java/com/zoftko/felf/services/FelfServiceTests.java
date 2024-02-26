package com.zoftko.felf.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.zoftko.felf.config.CacheConfiguration;
import com.zoftko.felf.dao.InstallationRepository;
import com.zoftko.felf.dao.ProjectRepository;
import com.zoftko.felf.entities.Installation;
import com.zoftko.felf.entities.Project;
import com.zoftko.felf.models.Repository;
import com.zoftko.felf.models.RepositoryInstallation;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import reactor.core.publisher.Mono;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(classes = { FelfService.class, CacheConfiguration.class, CacheAutoConfiguration.class })
class FelfServiceTests {

    @MockBean
    GithubService githubService;

    @MockBean
    InstallationRepository installationRepository;

    @MockBean
    ProjectRepository projectRepository;

    @Autowired
    FelfService felfService;

    @Test
    void evictOnStoringProject() {
        var project = new Project();
        var owner = "crazybolillo";
        var repo = "felf";
        var name = String.join("/", owner, repo);

        when(githubService.getRepositoryInstallation(anyString(), anyString()))
            .thenReturn(Mono.just(new RepositoryInstallation(123, Map.of(), null)));
        when(githubService.getRepository(anyInt(), anyString(), anyString()))
            .thenReturn(Mono.just(new Repository(name, false, "main")));
        when(installationRepository.findByAccountLogin(anyString()))
            .thenReturn(Optional.of(new Installation()));
        when(projectRepository.findByFullName(anyString())).thenReturn(Optional.of(new Project()));

        assertThat(felfService.getProjectData(owner, repo))
            .isEqualTo(felfService.getProjectData(owner, repo));
        verify(projectRepository, times(1)).findByFullName(name);

        felfService.getFreshProjectData(owner, repo);
        verify(projectRepository, times(2)).findByFullName(name);

        project.setFullName("other/repo");
        felfService.storeProject(project);
        felfService.getProjectData(owner, repo);
        felfService.getProjectData("other", "repo");

        verify(projectRepository, times(2)).findByFullName(name);
        verify(projectRepository, times(3)).findByFullName(anyString());

        project.setFullName(name);
        felfService.storeProject(project);
        felfService.getProjectData(owner, repo);
        verify(projectRepository, times(3)).findByFullName(name);
    }

    @Test
    void getRepositoryInstallationWithError(CapturedOutput output) {
        when(githubService.getRepositoryInstallation(anyString(), anyString()))
            .thenReturn(Mono.error(new Exception("kaboom")));

        var projectData = felfService.getProjectData("zoftko", "felf");

        assertThat(projectData.repoInstall()).isNull();
        assertThat(output.getOut()).contains("kaboom");
    }
}
