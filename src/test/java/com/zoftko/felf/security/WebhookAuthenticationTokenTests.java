package com.zoftko.felf.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.zoftko.felf.security.WebhookAuthenticationToken;
import org.junit.jupiter.api.Test;

class WebhookAuthenticationTokenTests {

    @Test
    void testEquals() {
        assertThat(new WebhookAuthenticationToken("kiwi").equals(new WebhookAuthenticationToken("kiwi")))
            .isTrue();
    }

    @Test
    void testNotEquals() {
        assertThat(new WebhookAuthenticationToken("hello").equals(new WebhookAuthenticationToken("bye")))
            .isFalse();
    }
}
