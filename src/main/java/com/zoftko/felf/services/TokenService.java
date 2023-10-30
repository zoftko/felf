package com.zoftko.felf.services;

import reactor.core.publisher.Mono;

public interface TokenService<T> {
    public Mono<String> token(T id);

    public Mono<String> renewToken(T id);
}
