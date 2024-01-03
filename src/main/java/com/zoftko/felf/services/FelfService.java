package com.zoftko.felf.services;

import com.zoftko.felf.dao.InstallationRepository;
import com.zoftko.felf.dao.ProjectRepository;
import com.zoftko.felf.entities.Project;
import com.zoftko.felf.models.ProjectData;
import com.zoftko.felf.models.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@CacheConfig(cacheNames = FelfService.CACHE_NAME)
public class FelfService {

    public static final String CACHE_NAME = "felf-service";

    private final InstallationRepository installationRepository;
    private final ProjectRepository projectRepository;
    private final GithubService githubService;

    @Autowired
    public FelfService(
        InstallationRepository installationRepository,
        ProjectRepository projectRepository,
        GithubService githubService
    ) {
        this.installationRepository = installationRepository;
        this.projectRepository = projectRepository;
        this.githubService = githubService;
    }

    private ProjectData computeProjectData(String owner, String repo) {
        var fullName = owner + "/" + repo;
        var project = projectRepository.findByFullName(fullName);
        var installation = installationRepository.findByAccountLogin(owner);
        var repoInstall = githubService
            .getRepositoryInstallation(owner, repo)
            .onErrorResume(__ -> Mono.empty())
            .block();

        Repository repository = null;
        if (repoInstall != null) {
            repository =
                githubService
                    .getRepository(repoInstall.id(), owner, repo)
                    .onErrorResume(__ -> Mono.empty())
                    .block();
        }

        return new ProjectData(fullName, project, installation, repoInstall, repository);
    }

    @CacheEvict(key = "#project.fullName")
    public void storeProject(Project project) {
        this.projectRepository.save(project);
    }

    @Cacheable(key = "#owner + '/' + #repo")
    public ProjectData getProjectData(String owner, String repo) {
        return computeProjectData(owner, repo);
    }

    @CachePut(key = "#owner + '/' + #repo")
    public ProjectData getFreshProjectData(String owner, String repo) {
        return computeProjectData(owner, repo);
    }
}
