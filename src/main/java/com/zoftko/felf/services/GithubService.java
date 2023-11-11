package com.zoftko.felf.services;

import com.zoftko.felf.dao.InstallationRepository;
import com.zoftko.felf.dao.ProjectRepository;
import com.zoftko.felf.entities.Installation;
import com.zoftko.felf.entities.Project;
import com.zoftko.felf.models.InstallationRepos;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class GithubService {

    public static final String HTTP_HEADER_GH_UID = "X-Github-Uid";
    public static final String QUALIFIER_INSTALL_TOKEN = "gh-install";
    public static final String QUALIFIER_APP_TOKEN = "gh-app";
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 30;

    private final InstallationRepository installationRepository;
    private final ProjectRepository projectRepository;
    private final WebClient client;

    @Autowired
    public GithubService(
        InstallationRepository installationRepository,
        ProjectRepository projectRepository,
        @Qualifier(QUALIFIER_INSTALL_TOKEN) WebClient client
    ) {
        this.client = client;
        this.projectRepository = projectRepository;
        this.installationRepository = installationRepository;
    }

    public List<Installation> getUserInstallations(int id) {
        return installationRepository.findBySender(id);
    }

    public boolean userForbiddenFromInstallation(int userId, int installationId) {
        return installationRepository.findBySenderAndId(userId, installationId) == null;
    }

    public List<Project> getInstallationProjects(int installationId) {
        return projectRepository.findByInstallationId(installationId);
    }

    public Mono<InstallationRepos> getInstallationRepositories(int installation, int page, int perPage) {
        return client
            .get()
            .uri(builder ->
                builder
                    .path("/installation/repositories")
                    .queryParam("page", page)
                    .queryParam("per_page", perPage)
                    .build()
            )
            .header(HTTP_HEADER_GH_UID, Integer.toString(installation))
            .retrieve()
            .bodyToMono(InstallationRepos.class)
            .cache();
    }
}
