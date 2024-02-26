package com.zoftko.felf.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.zoftko.felf.dao.InstallationRepository;
import com.zoftko.felf.dao.ProjectRepository;
import com.zoftko.felf.entities.Installation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {

    public static final String ACTION_KEY = "action";

    public static final String EVENT_INSTALLATION_REPOS = "installation_repositories";
    public static final String EVENT_INSTALLATION = "installation";
    public static final String EVENT_REPOSITORY = "repository";

    public static final String ACTION_DELETED = "deleted";
    public static final String ACTION_CREATED = "created";
    public static final String ACTION_EDITED = "edited";
    public static final String ACTION_PRIVATIZED = "privatized";
    public static final String ACTION_PUBLICIZED = "publicized";

    public static final String PROCESS_TMPL = "processing {}.{}";

    private final InstallationRepository installationRepository;
    private final ProjectRepository projectRepository;
    private final CacheManager cacheManager;

    private final Logger log = LoggerFactory.getLogger(WebhookService.class);

    @Autowired
    public WebhookService(
        InstallationRepository installationRepository,
        ProjectRepository projectRepository,
        CacheManager cacheManager
    ) {
        this.installationRepository = installationRepository;
        this.projectRepository = projectRepository;
        this.cacheManager = cacheManager;
    }

    private Installation jsonToInstallation(JsonNode json) {
        JsonNode installation = json.get(EVENT_INSTALLATION);

        int installationId = installation.get("id").asInt();
        int accountId = installation.get("account").get("id").asInt();
        int targetId = installation.get("target_id").asInt();
        int senderId = json.get("sender").get("id").asInt();
        String targetType = installation.get("target_type").asText();
        String accountLogin = installation.get("account").get("login").asText();

        return new Installation(installationId, accountId, targetId, targetType, accountLogin, senderId);
    }

    private void processInstallationEvent(JsonNode payload) {
        String action = payload.path(ACTION_KEY).asText("none");
        log.info(PROCESS_TMPL, EVENT_INSTALLATION, action);

        switch (action) {
            case ACTION_DELETED -> installationRepository.deleteById(
                payload.path(EVENT_INSTALLATION).path("id").asInt(-1)
            );
            case ACTION_CREATED -> {
                try {
                    installationRepository.save(jsonToInstallation(payload));
                } catch (NullPointerException exception) {
                    log.error("invalid payload for {}.{}", EVENT_INSTALLATION, action);
                }
            }
            default -> log.info("{}.{} is not supported", EVENT_INSTALLATION, action);
        }
    }

    private void processRepositoryEvent(JsonNode payload) {
        String action = payload.path(ACTION_KEY).asText("none");
        String fullName = payload.path(EVENT_REPOSITORY).path("full_name").asText();

        log.info(PROCESS_TMPL, EVENT_REPOSITORY, action);
        switch (action) {
            case ACTION_EDITED -> {
                String defaultBranch = payload.path(EVENT_REPOSITORY).path("default_branch").asText("");
                projectRepository.updateDefaultBranchByFullName(fullName, defaultBranch);
            }
            case ACTION_PRIVATIZED -> projectRepository.updateIsPrivateByFullName(fullName, true);
            case ACTION_PUBLICIZED -> projectRepository.updateIsPrivateByFullName(fullName, false);
            default -> log.info("{}.{} is not supported", EVENT_REPOSITORY, action);
        }
    }

    private void processInstallationReposEvent(JsonNode payload) {
        String action = payload.path(ACTION_KEY).asText("none");
        log.info(PROCESS_TMPL, EVENT_INSTALLATION_REPOS, action);

        var repos = payload.get(String.format("repositories_%s", action));
        var cache = cacheManager.getCache(FelfService.CACHE_NAME);
        if (repos != null && cache != null) {
            repos.forEach(node -> cache.evictIfPresent(node.path("full_name").asText("")));
        }
    }

    @Async
    public void processEvent(String event, JsonNode payload) {
        switch (event) {
            case EVENT_INSTALLATION -> processInstallationEvent(payload);
            case EVENT_REPOSITORY -> processRepositoryEvent(payload);
            case EVENT_INSTALLATION_REPOS -> processInstallationReposEvent(payload);
            default -> log.info("event {} is not supported", event);
        }
    }
}
