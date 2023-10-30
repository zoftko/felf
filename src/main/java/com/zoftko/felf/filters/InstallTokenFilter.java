package com.zoftko.felf.filters;

import com.zoftko.felf.services.GithubService;
import com.zoftko.felf.services.InstallTokenService;
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
@Qualifier(GithubService.QUALIFIER_INSTALL_TOKEN)
public class InstallTokenFilter implements ExchangeFilterFunction {

    private final InstallTokenService tokenService;

    @Autowired
    public InstallTokenFilter(InstallTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        String uid = request.headers().getFirst(GithubService.HTTP_HEADER_GH_UID);
        if (uid == null) {
            return next.exchange(request);
        }

        return tokenService
            .token(Integer.parseInt(uid))
            .flatMap(token ->
                next.exchange(
                    ClientRequest.from(request).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).build()
                )
            )
            .flatMap(response -> {
                if (response.statusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
                    return response
                        .releaseBody()
                        .then(tokenService.renewToken(Integer.parseInt(uid)))
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
