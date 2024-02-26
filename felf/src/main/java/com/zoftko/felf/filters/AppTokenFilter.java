package com.zoftko.felf.filters;

import com.zoftko.felf.services.AppTokenService;
import com.zoftko.felf.services.GithubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@Component
@Qualifier(GithubService.QUALIFIER_APP_TOKEN)
public class AppTokenFilter implements ExchangeFilterFunction {

    private final AppTokenService tokenService;

    @Autowired
    public AppTokenFilter(AppTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return tokenService
            .token(null)
            .flatMap(token ->
                next.exchange(
                    ClientRequest.from(request).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).build()
                )
            )
            .flatMap(response -> {
                if (response.statusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
                    return response
                        .releaseBody()
                        .then(tokenService.renewToken(null))
                        .flatMap(token ->
                            next.exchange(
                                ClientRequest.from(request).header("Authorization", "Bearer " + token).build()
                            )
                        );
                } else {
                    return Mono.just(response);
                }
            });
    }
}
