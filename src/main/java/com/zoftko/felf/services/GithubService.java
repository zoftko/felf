package com.zoftko.felf.services;

import com.zoftko.felf.models.RepositoryInstallation;
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

    private final WebClient appClient;

    @Autowired
    public GithubService(@Qualifier(QUALIFIER_APP_TOKEN) WebClient appClient) {
        this.appClient = appClient;
    }

    public Mono<RepositoryInstallation> getRepositoryInstallation(String owner, String repo) {
        return appClient
            .get()
            .uri(builder -> builder.pathSegment("repos", owner, repo, "installation").build())
            .retrieve()
            .bodyToMono(RepositoryInstallation.class);
    }
}
