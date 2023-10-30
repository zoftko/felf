package com.zoftko.felf.ut.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.zoftko.felf.security.WebhookSecretFilter;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class WebhookSignatureTests {

    WebhookSecretFilter filter;

    @Test
    void githubExampleCase() {
        // Example test case as provided by GitHub
        // https://docs.github.com/en/webhooks/using-webhooks/validating-webhook-deliveries#testing-the-webhook-payload-validation
        filter = new WebhookSecretFilter("It's a Secret to Everybody");
        assertThat(
            filter.isValidSignature(
                "sha256=757107ea0eb2509fc211221cce984b8a37570b6d7586c22c46f4379c8b043e17",
                "Hello, World!".getBytes(StandardCharsets.UTF_8)
            )
        )
            .isTrue();
    }
}
