package com.zoftko.felf.security;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zoftko.felf.config.SecurityConfiguration;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class RedirectLoginSuccessHandlerTests {

    @Mock
    HttpServletRequest request;

    @Mock
    HttpSession session;

    @Mock
    HttpServletResponse response;

    @Mock
    Authentication authentication;

    @Test
    void withSessionAttribute() throws ServletException, IOException {
        var redirectURL = "https://mypage.com/interesting/page";

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(SecurityConfiguration.SESSION_REDIRECT_ATTR)).thenReturn(redirectURL);
        when(response.encodeRedirectURL(redirectURL)).thenReturn(redirectURL);

        new SecurityConfiguration.RedirectLoginSuccessHandler("/")
            .onAuthenticationSuccess(request, response, authentication);

        verify(response, times(1)).sendRedirect(redirectURL);
        verify(session, times(1)).removeAttribute(SecurityConfiguration.SESSION_REDIRECT_ATTR);
    }

    @Test
    void noSessionAttribute() throws ServletException, IOException {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(SecurityConfiguration.SESSION_REDIRECT_ATTR)).thenReturn(null);

        new SecurityConfiguration.RedirectLoginSuccessHandler("/")
            .onAuthenticationSuccess(request, response, authentication);

        verify(session, times(0)).removeAttribute(SecurityConfiguration.SESSION_REDIRECT_ATTR);
    }
}
