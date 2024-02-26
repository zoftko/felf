package com.zoftko.felf.security;

import java.util.Objects;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

public class WebhookAuthenticationToken extends AbstractAuthenticationToken {

    public static final String WEBHOOK_AUTHORITY = "WEBHOOK";

    private final String hookId;

    public WebhookAuthenticationToken(String hookId) {
        super(AuthorityUtils.createAuthorityList(WEBHOOK_AUTHORITY));
        setAuthenticated(true);
        this.hookId = hookId;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return hookId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WebhookAuthenticationToken that = (WebhookAuthenticationToken) o;

        return Objects.equals(hookId, that.hookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), hookId);
    }
}
