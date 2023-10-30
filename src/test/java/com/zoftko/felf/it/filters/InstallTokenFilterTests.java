package com.zoftko.felf.it.filters;

import static org.assertj.core.api.Assertions.assertThat;

import com.zoftko.felf.services.GithubService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest
class InstallTokenFilterTests {

    static MockWebServer mockWebServer = new MockWebServer();

    @Autowired
    @Qualifier(GithubService.QUALIFIER_INSTALL_TOKEN)
    WebClient client;

    @TestConfiguration
    static class Configuration {

        @Bean
        @Primary
        @Qualifier("gh")
        WebClient.Builder builder() {
            return WebClient.builder().baseUrl(mockWebServer.url("/").toString());
        }
    }

    RecordedRequest takeAfter(int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            mockWebServer.takeRequest();
        }

        return mockWebServer.takeRequest();
    }

    void enqueueToken(String installToken) {
        mockWebServer.enqueue(
            new MockResponse()
                .setBody(String.format("{\"token\": \"%s\"}", installToken))
                .setHeader("Content-Type", "application/json")
        );
    }

    @Test
    void reuseToken() throws InterruptedException {
        enqueueToken("test-token");
        mockWebServer.enqueue(new MockResponse());

        client
            .get()
            .header(GithubService.HTTP_HEADER_GH_UID, "6124")
            .retrieve()
            .bodyToMono(String.class)
            .block();
        assertThat(takeAfter(0).getRequestUrl())
            .isEqualTo(mockWebServer.url("/app/installations/6124/access_tokens"));
        var requestToken = mockWebServer.takeRequest().getHeader(HttpHeaders.AUTHORIZATION);

        mockWebServer.enqueue(new MockResponse());
        client
            .get()
            .header(GithubService.HTTP_HEADER_GH_UID, "6124")
            .retrieve()
            .bodyToMono(String.class)
            .block();
        assertThat(requestToken).isEqualTo(mockWebServer.takeRequest().getHeader(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void renewTokenOnUnauthorized() throws InterruptedException {
        enqueueToken("randoturf");
        mockWebServer.enqueue(new MockResponse());

        client
            .get()
            .header(GithubService.HTTP_HEADER_GH_UID, "54321")
            .retrieve()
            .bodyToMono(String.class)
            .block();

        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.UNAUTHORIZED.value()));
        enqueueToken("watup");
        mockWebServer.enqueue(new MockResponse());

        client
            .get()
            .header(GithubService.HTTP_HEADER_GH_UID, "54321")
            .retrieve()
            .bodyToMono(String.class)
            .block();

        var originalToken = takeAfter(1).getHeader(HttpHeaders.AUTHORIZATION);
        var renewedToken = takeAfter(2).getHeader(HttpHeaders.AUTHORIZATION);

        assertThat(originalToken).contains("randoturf");
        assertThat(renewedToken).contains("watup");
        assertThat(originalToken).isNotEqualTo(renewedToken);
    }

    @Test
    void noHeaderGhUid() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse());
        client.get().retrieve().bodyToMono(String.class).block();

        assertThat(mockWebServer.takeRequest().getHeader(HttpHeaders.AUTHORIZATION)).isNull();
    }
}
