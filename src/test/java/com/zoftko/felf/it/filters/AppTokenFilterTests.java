package com.zoftko.felf.it.filters;

import static org.assertj.core.api.Assertions.assertThat;

import com.zoftko.felf.services.GithubService;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest
class AppTokenFilterTests {

    MockWebServer mockWebServer = new MockWebServer();

    @Autowired
    @Qualifier(GithubService.QUALIFIER_APP_TOKEN)
    WebClient client;

    @BeforeEach
    void setUp() {
        client = client.mutate().baseUrl(mockWebServer.url("/").toString()).build();
    }

    @Test
    void reuseAppToken() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse());
        client.get().retrieve().bodyToMono(String.class).block();

        var appToken = mockWebServer.takeRequest().getHeader(HttpHeaders.AUTHORIZATION);

        mockWebServer.enqueue(new MockResponse());
        client.get().retrieve().bodyToMono(String.class).block();

        assertThat(appToken).isEqualTo(mockWebServer.takeRequest().getHeader(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void renewTokenOnUnauthorized() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse());
        client.get().retrieve().bodyToMono(String.class).block();

        var appToken = mockWebServer.takeRequest().getHeader(HttpHeaders.AUTHORIZATION);

        mockWebServer.enqueue(new MockResponse().setResponseCode(401).setHeadersDelay(1, TimeUnit.SECONDS));
        mockWebServer.enqueue(new MockResponse());
        client.get().retrieve().bodyToMono(String.class).block();
        mockWebServer.takeRequest();

        assertThat(appToken).isNotEqualTo(mockWebServer.takeRequest().getHeader(HttpHeaders.AUTHORIZATION));
    }
}
