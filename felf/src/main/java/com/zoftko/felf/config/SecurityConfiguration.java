package com.zoftko.felf.config;

import static com.zoftko.felf.controllers.StaticController.LOGIN_MAPPING;
import static org.springframework.security.config.Customizer.withDefaults;

import com.zoftko.felf.controllers.AnalysisController;
import com.zoftko.felf.controllers.WebhookController;
import com.zoftko.felf.security.WebhookAuthenticationToken;
import com.zoftko.felf.security.WebhookSecretFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SecureRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    public static final String SESSION_REDIRECT_ATTR = "RedirectOnLogin";
    public static final String AUTHORITY_OAUTH2_USER = "OAUTH2_USER";

    @Value("${felf.github.app.webhook.secret}")
    private String ghWebhookSecret;

    public static class RedirectLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

        public RedirectLoginSuccessHandler(String defaultURL) {
            setDefaultTargetUrl(defaultURL);
        }

        @Override
        public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
        ) throws ServletException, IOException {
            String redirectURL = (String) request.getSession().getAttribute(SESSION_REDIRECT_ATTR);
            if (redirectURL != null && !redirectURL.isEmpty()) {
                request.getSession().removeAttribute(SESSION_REDIRECT_ATTR);
                getRedirectStrategy().sendRedirect(request, response, redirectURL);
            } else {
                super.onAuthenticationSuccess(request, response, authentication);
            }
        }
    }

    public LogoutSuccessHandler logoutHandler() {
        var handler = new SimpleUrlLogoutSuccessHandler();
        handler.setDefaultTargetUrl("/");
        handler.setUseReferer(true);

        return handler;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain analysisFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(AnalysisController.MAPPING)
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain webhookFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(WebhookController.MAPPING)
            .authorizeHttpRequests(authorize ->
                authorize.anyRequest().hasAuthority(WebhookAuthenticationToken.WEBHOOK_AUTHORITY)
            )
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .addFilterBefore(new WebhookSecretFilter(ghWebhookSecret), SecurityContextHolderFilter.class);

        return http.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .csrf(withDefaults())
            .oauth2Login(oauth2 ->
                oauth2.loginPage(LOGIN_MAPPING).successHandler(new RedirectLoginSuccessHandler("/"))
            )
            .logout(logout -> logout.logoutSuccessHandler(logoutHandler()));

        return http.build();
    }

    @Bean
    SecureRandom secureRandom() {
        return new SecureRandom();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
