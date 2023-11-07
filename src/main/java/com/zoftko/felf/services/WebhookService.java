package com.zoftko.felf.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.zoftko.felf.dao.InstallationRepository;
import com.zoftko.felf.entities.Installation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {

    public static final String EVENT_INSTALLATION = "installation";
    public static final String ACTION_DELETED = "deleted";
    public static final String ACTION_CREATED = "created";

    private final InstallationRepository installationRepository;

    private final Logger log = LoggerFactory.getLogger(WebhookService.class);

    @Autowired
    public WebhookService(InstallationRepository installationRepository) {
        this.installationRepository = installationRepository;
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
        String action = payload.path("action").asText("none");
        switch (action) {
            case ACTION_DELETED -> installationRepository.deleteById(
                payload.path(EVENT_INSTALLATION).path("id").asInt(-1)
            );
            case ACTION_CREATED -> {
                try {
                    installationRepository.save(jsonToInstallation(payload));
                } catch (NullPointerException exception) {
                    log.error("invalid payload for installation action '{}' received", action);
                }
            }
            default -> log.info("unknown installation action '{}' provided", action);
        }
    }

    @Async
    public void processEvent(String event, JsonNode payload) {
        if (event.equals(EVENT_INSTALLATION)) {
            processInstallationEvent(payload);
        }
    }
}
