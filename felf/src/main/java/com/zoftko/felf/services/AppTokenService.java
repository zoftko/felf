package com.zoftko.felf.services;

import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
@CacheConfig(cacheNames = "gh-app-tokens")
public class AppTokenService implements TokenService<String> {

    @Value("${felf.github.app.pem}")
    private String githubPem;

    @Value("${felf.github.app.id}")
    private String githubAppId;

    private Key githubKey;

    private String generateToken() {
        return Jwts
            .builder()
            .claim("alg", "RS256")
            .claim("iss", githubAppId)
            .claim("iat", (int) Instant.now().minusSeconds(60).getEpochSecond())
            .claim("exp", (int) Instant.now().plusSeconds(300).getEpochSecond())
            .signWith(githubKey)
            .compact();
    }

    @PostConstruct
    public void init() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        try (
            var pemReader = new PemReader(
                new InputStreamReader(new ByteArrayInputStream(Base64.getDecoder().decode(githubPem)))
            )
        ) {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pemReader.readPemObject().getContent());
            githubKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        }
    }

    @Override
    @Cacheable
    public Mono<String> token(String id) {
        return Mono.just(generateToken());
    }

    @Override
    @CachePut
    public Mono<String> renewToken(String id) {
        return Mono.just(generateToken());
    }
}
