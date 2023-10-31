package com.zoftko.felf.ut.services;

import static com.zoftko.felf.services.WebhookService.*;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@DataJpaTest
@ExtendWith(OutputCaptureExtension.class)
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
        int installationId,
        int accountId,
        int targetId,
        int senderId,
        String accountLogin
    ) throws JSONException, JsonProcessingException {
        JSONObject payload = new JSONObject()
            .put("action", WebhookService.ACTION_CREATED)
            .put(
                "installation",
                new JSONObject()
                    .put("id", installationId)
                    .put("account", new JSONObject().put("id", accountId).put("login", accountLogin))
                    .put("target_id", targetId)
                    .put("target_type", "User")
            )
            .put("sender", new JSONObject().put("id", senderId));

        return objectMapper.readTree(payload.toString());
    }

    @Test
    void processInstallationInstalled() throws JSONException, JsonProcessingException {
        int installationId = 12345;
        assertThat(installationRepository.findById(installationId)).isNotPresent();

        webhookService.processEvent(
            WebhookService.EVENT_INSTALLATION,
            createInstallationJsonNode(installationId, 1123, 42312, 56, "bach")
        );
        var installationOpt = installationRepository.findById(installationId);
        assertThat(installationOpt).isPresent();

        var installation = installationOpt.get();
        assertThat(installation.getSender()).isEqualTo(56);
        assertThat(installation.getAccount()).isEqualTo(1123);
        assertThat(installation.getTarget()).isEqualTo(42312);
        assertThat(installation.getTargetType()).isEqualTo("User");
        assertThat(installation.getAccountLogin()).isEqualTo("bach");
    }

    @Test
    void processInstallationDeleted() throws JSONException, JsonProcessingException {
        int installationId = 98765;
        assertThat(installationRepository.findById(installationId)).isNotPresent();

        webhookService.processEvent(
            WebhookService.EVENT_INSTALLATION,
            createInstallationJsonNode(installationId, 1234, 5678, 987, "anton")
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

    @Test
    void processBadPayload(CapturedOutput output) throws JSONException, JsonProcessingException {
        int installationId = 12345;
        webhookService.processEvent(
            EVENT_INSTALLATION,
            objectMapper.readTree(
                new JSONObject()
                    .put("action", ACTION_CREATED)
                    .put("installation", new JSONObject().put("what", "boom").put("id", installationId))
                    .toString()
            )
        );

        assertThat(output.getOut()).contains("invalid payload for installation action");
        assertThat(installationRepository.findById(installationId)).isNotPresent();
    }

    @Test
    void processBadAction(CapturedOutput output) throws JSONException, JsonProcessingException {
        webhookService.processEvent(
            EVENT_INSTALLATION,
            objectMapper.readTree(new JSONObject().put("action", "bazinga").toString())
        );

        assertThat(output.getOut()).contains("unknown installation action");
    }
}
