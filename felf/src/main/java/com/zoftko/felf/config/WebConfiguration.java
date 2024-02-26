package com.zoftko.felf.config;

import com.zoftko.felf.services.GithubService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Bean(name = "gh")
    public WebClient.Builder defaultGithubBuilder() {
        return WebClient
            .builder()
            .baseUrl("https://api.github.com")
            .defaultHeader("X-Gtihub-Api-Version", "2022-11-28")
            .defaultHeader("Accept", "application/vnd.github+json");
    }

    @Bean(name = GithubService.QUALIFIER_APP_TOKEN)
    WebClient githubAppClient(
        @Qualifier("gh") WebClient.Builder defaultGithubBuilder,
        @Qualifier(GithubService.QUALIFIER_APP_TOKEN) ExchangeFilterFunction exchangeFilterFunction
    ) {
        return defaultGithubBuilder.clone().filter(exchangeFilterFunction).build();
    }

    @Bean(name = GithubService.QUALIFIER_INSTALL_TOKEN)
    WebClient githubInstallClient(
        @Qualifier("gh") WebClient.Builder defaultGithubBuilder,
        @Qualifier(GithubService.QUALIFIER_INSTALL_TOKEN) ExchangeFilterFunction exchangeFilterFunction
    ) {
        return defaultGithubBuilder.clone().filter(exchangeFilterFunction).build();
    }
}
