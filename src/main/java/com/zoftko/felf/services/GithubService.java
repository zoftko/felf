package com.zoftko.felf.services;

import com.zoftko.felf.models.CreateIssueComment;
import com.zoftko.felf.models.Repository;
import com.zoftko.felf.models.RepositoryInstallation;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

@Service
public class GithubService {

    public static final String HTTP_HEADER_GH_UID = "X-Github-Uid";
    public static final String QUALIFIER_INSTALL_TOKEN = "githubInstall";
    public static final String QUALIFIER_APP_TOKEN = "githubApp";

    private final WebClient appClient;
    private final WebClient installClient;

    @Autowired
    public GithubService(
        @Qualifier(QUALIFIER_APP_TOKEN) WebClient appClient,
        @Qualifier(QUALIFIER_INSTALL_TOKEN) WebClient installClient
    ) {
        this.appClient = appClient;
        this.installClient = installClient;
    }

    UriBuilder repoPath(UriBuilder builder, String owner, String repo, String... segments) {
        return builder.pathSegment("repos", owner, repo).pathSegment(segments);
    }

    public Mono<RepositoryInstallation> getRepositoryInstallation(String owner, String repo) {
        return appClient
            .get()
            .uri(builder -> repoPath(builder, owner, repo, "installation").build())
            .retrieve()
            .bodyToMono(RepositoryInstallation.class);
    }

    public Mono<Repository> getRepository(int installationId, String owner, String repo) {
        return installClient
            .get()
            .uri(builder -> repoPath(builder, owner, repo).build())
            .header(HTTP_HEADER_GH_UID, Integer.toString(installationId))
            .retrieve()
            .bodyToMono(Repository.class);
    }

    public Mono<CreateIssueComment> createIssueComment(
        int installationId,
        String issueId,
        String owner,
        String repo,
        String body
    ) {
        var payload = new JSONObject();
        payload.put("body", body);

        return installClient
            .post()
            .uri(builder -> repoPath(builder, owner, repo, "issues", issueId, "comments").build())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HTTP_HEADER_GH_UID, Integer.toString(installationId))
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(CreateIssueComment.class);
    }

    public Mono<ResponseEntity<Void>> deleteIssueComment(
        int installationId,
        String owner,
        String repo,
        long commentId
    ) {
        return installClient
            .delete()
            .uri(builder ->
                repoPath(builder, owner, repo, "issues", "comments", Long.toString(commentId)).build()
            )
            .header(HTTP_HEADER_GH_UID, Integer.toString(installationId))
            .retrieve()
            .toBodilessEntity();
    }
}
