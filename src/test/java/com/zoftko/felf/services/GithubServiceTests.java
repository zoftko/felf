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
        service = new GithubService(client);
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
}
