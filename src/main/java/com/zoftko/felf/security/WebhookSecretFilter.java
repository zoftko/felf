package com.zoftko.felf.security;

import com.zoftko.felf.http.CachedHttpServletRequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

public class WebhookSecretFilter extends OncePerRequestFilter {

    public static final String HEADER_SIGNATURE = "X-Hub-Signature-256";
    public static final String HEADER_HOOK_ID = "X-Github-Hook-ID";

    private final SecretKey secretKey;
    private final SecurityContextHolderStrategy securityContextHolderStrategy =
        SecurityContextHolder.getContextHolderStrategy();

    private final SecurityContextRepository securityContextRepository =
        new RequestAttributeSecurityContextRepository();

    private final Logger log = LoggerFactory.getLogger(WebhookSecretFilter.class);

    public WebhookSecretFilter(String secret) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public String generateSignature(byte[] payload) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);

        return new String(Hex.encode(mac.doFinal(payload)));
    }

    public boolean isValidSignature(String signature, byte[] payload) {
        try {
            return ("sha256=" + generateSignature(payload)).equals(signature);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            return false;
        }
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String signature = request.getHeader(HEADER_SIGNATURE);
        if (signature == null) {
            log.info("no {} header found, rejecting request", HEADER_SIGNATURE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        CachedHttpServletRequestWrapper requestWrapper = new CachedHttpServletRequestWrapper(request);
        var payload = requestWrapper.getInputStream().readAllBytes();
        if (!isValidSignature(signature, payload)) {
            log.info("invalid {} provided, rejecting request", HEADER_SIGNATURE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        var context = securityContextHolderStrategy.getContext();
        context.setAuthentication(new WebhookAuthenticationToken(request.getHeader(HEADER_HOOK_ID)));
        securityContextRepository.saveContext(context, request, response);
        filterChain.doFilter(requestWrapper, response);
    }
}
