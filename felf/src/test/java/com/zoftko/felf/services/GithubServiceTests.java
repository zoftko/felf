package com.zoftko.felf.services;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class GithubServiceTests {

    WebClient client;

    GithubService service;
    MockWebServer server = new MockWebServer();

    @BeforeEach
    void setUp() {
        client = WebClient.builder().baseUrl(server.url("/").toString()).build();
        service = new GithubService(client, client);
    }

    @Test
    void getRepositoryInstallation() throws InterruptedException {
        server.enqueue(new MockResponse());
        service.getRepositoryInstallation("crazybolillo", "dotconfig").block();

        var request = server.takeRequest();
        assertThat(request.getRequestUrl().encodedPath())
            .isEqualTo("/repos/crazybolillo/dotconfig/installation");
        assertThat(request.getMethod()).isEqualToIgnoringCase("GET");
    }

    @Test
    void getRepository() throws InterruptedException {
        server.enqueue(new MockResponse());
        service.getRepository(12345, "zoftko", "felf").block();

        var request = server.takeRequest();
        assertThat(request.getRequestUrl().encodedPath()).isEqualTo("/repos/zoftko/felf");
        assertThat(request.getHeader(GithubService.HTTP_HEADER_GH_UID)).isEqualTo("12345");
        assertThat(request.getMethod()).isEqualToIgnoringCase("GET");
    }

    @Test
    void createIssueComment() throws InterruptedException {
        server.enqueue(new MockResponse());
        service.createIssueComment(123, "57", "zoftko", "felf", "kiwiblue").block();

        var request = server.takeRequest();
        assertThat(request.getRequestUrl().encodedPath()).isEqualTo("/repos/zoftko/felf/issues/57/comments");
        assertThat(request.getHeader(GithubService.HTTP_HEADER_GH_UID)).isEqualTo("123");
        assertThat(request.getMethod()).isEqualToIgnoringCase("POST");
    }

    @Test
    void deleteIssueComment() throws InterruptedException {
        server.enqueue(new MockResponse());
        service.deleteIssueComment(532, "kiwi", "blue", 12312L).block();

        var request = server.takeRequest();
        assertThat(request.getRequestUrl().encodedPath()).isEqualTo("/repos/kiwi/blue/issues/comments/12312");
        assertThat(request.getHeader(GithubService.HTTP_HEADER_GH_UID)).isEqualTo("532");
        assertThat(request.getMethod()).isEqualToIgnoringCase("DELETE");
    }
}
