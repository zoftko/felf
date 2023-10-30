package com.zoftko.felf.services;

import com.zoftko.felf.models.GhInstallToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@CacheConfig(cacheNames = "gh-install-tokens")
public class InstallTokenService {

    private final WebClient webClient;

    @Autowired
    public InstallTokenService(@Qualifier(GithubService.QUALIFIER_APP_TOKEN) WebClient webClient) {
        this.webClient = webClient;
    }

    private Mono<String> generateToken(Integer id) {
        return webClient
            .post()
            .uri(uriBuilder -> uriBuilder.path("/app/installations/{installation_id}/access_tokens").build(id)
            )
            .retrieve()
            .bodyToMono(GhInstallToken.class)
            .flatMap(token -> Mono.just(token.token()))
            .cache();
    }

    @Cacheable
    public Mono<String> token(Integer id) {
        return generateToken(id);
    }

    @CachePut
    public Mono<String> renewToken(Integer id) {
        return generateToken(id);
    }
}
