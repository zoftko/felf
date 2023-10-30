package com.zoftko.felf.security;

import com.zoftko.felf.controllers.WebhookController;
import com.zoftko.felf.security.WebhookSecretFilter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebhookSecretFilterTests {

    @Value("${felf.github.app.webhook.secret}")
    String webhookSecret;

    @LocalServerPort
    private int port;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        client =
            WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .defaultHeader(WebhookController.HEADER_EVENT, "just testing")
                .build();
    }

    @Test
    void requestValidSignature() throws JSONException, NoSuchAlgorithmException, InvalidKeyException {
        WebhookSecretFilter filter = new WebhookSecretFilter(webhookSecret);

        String payload = new JSONObject().put("value", "hello").toString();
        String signature = "sha256=" + filter.generateSignature(payload.getBytes(StandardCharsets.UTF_8));

        client
            .post()
            .uri(WebhookController.MAPPING)
            .header(WebhookSecretFilter.HEADER_SIGNATURE, signature)
            .bodyValue(payload)
            .exchange()
            .expectAll(spec -> spec.expectStatus().isOk());
    }

    @Test
    void requestInvalidSignature() {
        client
            .post()
            .uri(WebhookController.MAPPING)
            .header(WebhookSecretFilter.HEADER_SIGNATURE, "bazinga")
            .bodyValue("")
            .exchange()
            .expectAll(spec -> spec.expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void requestNoSignature() {
        client
            .post()
            .uri(WebhookController.MAPPING)
            .bodyValue("")
            .exchange()
            .expectAll(spec -> spec.expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED));
    }
}
