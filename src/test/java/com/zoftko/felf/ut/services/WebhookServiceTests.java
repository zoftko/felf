package com.zoftko.felf.ut.services;

import static com.zoftko.felf.services.WebhookService.ACTION_CREATED;
import static com.zoftko.felf.services.WebhookService.ACTION_DELETED;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoftko.felf.dao.InstallationRepository;
import com.zoftko.felf.services.WebhookService;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WebhookServiceTests {

    @Autowired
    InstallationRepository installationRepository;

    WebhookService webhookService;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        webhookService = new WebhookService(installationRepository);
    }

    JsonNode createInstallationJsonNode(
        String action,
        int installationId,
        int accountId,
        int targetId,
        String targetType
    ) throws JSONException, JsonProcessingException {
        JSONObject payload = new JSONObject()
            .put("action", action)
            .put(
                "installation",
                new JSONObject()
                    .put("id", installationId)
                    .put("account", new JSONObject().put("id", accountId))
                    .put("target_id", targetId)
                    .put("target_type", targetType)
            );

        return objectMapper.readTree(payload.toString());
    }

    @Test
    void processInstallationInstalled() throws JSONException, JsonProcessingException {
        int installationId = 12345;
        assertThat(installationRepository.findById(installationId)).isNotPresent();

        webhookService.processEvent(
            WebhookService.EVENT_INSTALLATION,
            createInstallationJsonNode(ACTION_CREATED, installationId, 1123, 42312, "User")
        );
        var installationOpt = installationRepository.findById(installationId);

        assertThat(installationOpt).isPresent();

        var installation = installationOpt.get();
        assertThat(installation.getAccount()).isEqualTo(1123);
        assertThat(installation.getTarget()).isEqualTo(42312);
        assertThat(installation.getTargetType()).isEqualTo("User");
    }

    @Test
    void processInstallationDeleted() throws JSONException, JsonProcessingException {
        int installationId = 98765;
        assertThat(installationRepository.findById(installationId)).isNotPresent();

        webhookService.processEvent(
            WebhookService.EVENT_INSTALLATION,
            createInstallationJsonNode(ACTION_CREATED, installationId, 1234, 5678, "User")
        );
        assertThat(installationRepository.findById(installationId)).isPresent();

        webhookService.processEvent(
            WebhookService.EVENT_INSTALLATION,
            objectMapper.readTree(
                new JSONObject()
                    .put("action", ACTION_DELETED)
                    .put("installation", new JSONObject().put("id", installationId))
                    .toString()
            )
        );
        assertThat(installationRepository.findById(installationId)).isNotPresent();
    }
}
