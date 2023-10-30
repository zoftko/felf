package com.zoftko.felf.config;

import static com.zoftko.felf.controllers.StaticController.LOGIN_MAPPING;
import static org.springframework.security.config.Customizer.withDefaults;

import com.zoftko.felf.controllers.WebhookController;
import com.zoftko.felf.security.WebhookAuthenticationToken;
import com.zoftko.felf.security.WebhookSecretFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Value("${felf.github.app.webhook.secret}")
    private String ghWebhookSecret;

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
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
            .authorizeHttpRequests(authorize ->
                authorize
                    .requestMatchers(LOGIN_MAPPING, "/static/**")
                    .permitAll()
                    .anyRequest()
                    .hasAuthority("OAUTH2_USER")
            )
            .csrf(withDefaults())
            .oauth2Login(oauth2 -> oauth2.loginPage(LOGIN_MAPPING).defaultSuccessUrl("/"))
            .logout(logout -> logout.logoutSuccessUrl(LOGIN_MAPPING));

        return http.build();
    }
}
