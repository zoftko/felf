package com.zoftko.felf.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.zoftko.felf.config.CacheConfiguration;
import com.zoftko.felf.dao.InstallationRepository;
import com.zoftko.felf.dao.ProjectRepository;
import com.zoftko.felf.entities.Installation;
import com.zoftko.felf.entities.Project;
import com.zoftko.felf.models.RepositoryInstallation;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;

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
}
